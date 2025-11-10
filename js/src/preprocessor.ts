import { BENEFITS_PLACEHOLDER_REGEX_PATTERN, QRCODE_PLACEHOLDER, BENEFITS_FIELD_NAME, FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN, DEFAULT_ENG, getLanguageCodes} from "./constants";
import { generateQRCode } from "@mosip/pixelpass";

interface MultiLineProperties {
  dataToSplit: string;
  placeholderList: string[];
  maxCharacterLength: number;
}

export async function preProcessVcJson(
  vcJsonString: string,
  svgTemplate: string,
  currentLanguage: string = DEFAULT_ENG,
  defaultLanguage: string = DEFAULT_ENG
): Promise<any> {
  const vcJsonObject = JSON.parse(vcJsonString);
  let credentialSubject =
    vcJsonObject.credential?.credentialSubject || vcJsonObject.credentialSubject;
  if (!credentialSubject) return vcJsonObject;

  credentialSubject = replaceFieldsWithLanguage(credentialSubject);

    // Checks for {{qrCodeImage}} for QR Code Replacement
  if (svgTemplate.includes(QRCODE_PLACEHOLDER)) {
    const qrCode = await replaceQRCode(vcJsonString);
    credentialSubject[getFieldNameFromPlaceholder(QRCODE_PLACEHOLDER)] = qrCode;
  }

    // Checks for benefits placeholders
  const benefitsPlaceholderRegex = new RegExp(BENEFITS_PLACEHOLDER_REGEX_PATTERN,  "g");
  if (benefitsPlaceholderRegex.test(svgTemplate)) {
    const benefitsPlaceholders = getPlaceholdersList(benefitsPlaceholderRegex, svgTemplate);
    const language = extractLanguageFromPlaceholder(benefitsPlaceholders[0]) || currentLanguage;
    const commaSeparatedBenefits = generateCommaSeparatedString(credentialSubject, [BENEFITS_FIELD_NAME], language, defaultLanguage);
    credentialSubject = constructObjectBasedOnCharacterLengthChunks(
      { dataToSplit: commaSeparatedBenefits, placeholderList: benefitsPlaceholders, maxCharacterLength: 55 },
      credentialSubject,
      language
    );
    delete credentialSubject[BENEFITS_FIELD_NAME];
  }

    // Checks for address placeholders
  const fullAddressRegex = new RegExp(FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN, "g");
  if (fullAddressRegex.test(svgTemplate)) {
    const addr = credentialSubject.address || {};
    const combinedAddress = [
      getLocalizedValue(addr.village, currentLanguage, defaultLanguage),
      getLocalizedValue(addr.district, currentLanguage, defaultLanguage),
      getLocalizedValue(addr.state, currentLanguage, defaultLanguage),
    ]
      .filter(Boolean)
      .join(", ");

    const fullAddressPlaceholders = getPlaceholdersList(fullAddressRegex, svgTemplate);
    credentialSubject = constructObjectBasedOnCharacterLengthChunks(
      { dataToSplit: combinedAddress, placeholderList: fullAddressPlaceholders, maxCharacterLength: 55 },
      credentialSubject,
      currentLanguage
    );
  }

  // 🌾 totalLandArea normalization
  if (credentialSubject.totalLandArea) {
    const area = credentialSubject.totalLandArea;
    credentialSubject.totalLandArea = {
      value: getLocalizedValue(area.value, currentLanguage, defaultLanguage),
      unit: getLocalizedValue(area.unit, currentLanguage, defaultLanguage),
    };
  }

  vcJsonObject.credentialSubject = credentialSubject;
  return vcJsonObject;
}

function replaceFieldsWithLanguage(jsonObject: any): any {
  if (!jsonObject || typeof jsonObject !== "object") return jsonObject;

  for (const key of Object.keys(jsonObject)) {
    const value = jsonObject[key];

    if (Array.isArray(value)) {
      const langMap: any = {};
      let hasLang = false;

      value.forEach((item) => {
        const lang = item.language || item["@language"];
        const val = item.value || item["@value"];
        if (lang && val !== undefined) {
          hasLang = true;
          langMap[lang] = val;
        }
      });

      if (hasLang) jsonObject[key] = langMap;
    } else if (typeof value === "object" && value !== null) {
      jsonObject[key] = replaceFieldsWithLanguage(value);
    }
  }
  return jsonObject;
}

function getLocalizedValue(
  value: any,
  language: string,
  defaultLanguage: string
): string | number | null {
  if (value == null) return null;

  if (typeof value === "object") {
    const languageCodes = [
      ...getLanguageCodes(language),
      ...getLanguageCodes(defaultLanguage),
      ...getLanguageCodes(DEFAULT_ENG),
    ];

    for (const code of languageCodes) {
      if (value[code]) return value[code];
    }

    const first = Object.values(value)[0];
    if (first) return first as string;
  }

  return value;
}

function getFieldNameFromPlaceholder(placeholder: string): string {
  const regex = new RegExp(GET_PLACEHOLDER_REGEX);
  const match = regex.exec(placeholder);
  const enclosedValue = match?.[1];
  return enclosedValue?.split("/").pop() || "";
}

/**
 * 🔍 Extract language suffix (e.g. /credentialSubject/name/en)
 */
function extractLanguageFromPlaceholder(placeholder: string): string {
  const regex = new RegExp(GET_LANGUAGE_FORM_PLACEHOLDER_REGEX);
  const match = regex.exec(placeholder);
  return match?.[1] || "";
}


export function getPlaceholdersList(
  placeholderRegexPattern: RegExp,
  svgTemplate: string
): string[] {
  const placeholders: string[] = [];
  for (const match of svgTemplate.matchAll(
    new RegExp(placeholderRegexPattern.source, "g")
  )) {
    placeholders.push(match[0]);
  }
  return placeholders;
}

async function replaceQRCode(vcJson: string): Promise<string> {
  try {
    const qrCode = await generateQRCode(vcJson);
    return qrCode;
  } catch (e) {
    console.error("QR generation failed:", e);
    return "";
  }
}

function generateCommaSeparatedString(
  jsonObject: any,
  fieldsToBeCombined: string[],
  currentLanguage: string,
  defaultLanguage: string
): string {
  return fieldsToBeCombined
    .flatMap((field) => {
      const fieldValue = jsonObject[field];
      if (Array.isArray(fieldValue)) {
        return fieldValue
          .map((item: any) =>
            typeof item === "string"
              ? item
              : getLocalizedValue(item, currentLanguage, defaultLanguage)
          )
          .filter(Boolean);
      } else if (typeof fieldValue === "object") {
        const val = getLocalizedValue(fieldValue, currentLanguage, defaultLanguage);
        return val ? [val] : [];
      }
      return fieldValue ? [fieldValue] : [];
    })
    .join(", ");
}

function constructObjectBasedOnCharacterLengthChunks(
  multiLineProperties: MultiLineProperties,
  jsonObject: any,
  language: string
): any {
  const segments = multiLineProperties.dataToSplit.match(new RegExp(`.{1,${multiLineProperties.maxCharacterLength}}`, "g")) || [];
  multiLineProperties.placeholderList.forEach((placeholder, index) => {
    if (index < segments.length) {
      jsonObject[getFieldNameFromPlaceholder(placeholder)] = language
        ? { [language]: segments[index] }
        : segments[index];
    }
  });
  return jsonObject;
}

const GET_PLACEHOLDER_REGEX = /{{credentialSubject\/([^/]+)(?:\/[^}]+)?}}/;
const GET_LANGUAGE_FORM_PLACEHOLDER_REGEX = /credentialSubject\/[^/]+\/(\w+)/;
