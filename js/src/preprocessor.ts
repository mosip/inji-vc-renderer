import { BENEFITS_PLACEHOLDER_REGEX_PATTERN, QRCODE_PLACEHOLDER, BENEFITS_FIELD_NAME, DEFAULT_ENG, getLanguageCodes} from "./constants";
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

  credentialSubject = normalizeLocalizedFields(credentialSubject);

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

  vcJsonObject.credentialSubject = credentialSubject;
  return vcJsonObject;
}

/**
 * Recursively normalize localized or nested objects
 * Supports all language formats:
 * - { "@language": "en", "@value": "John" }
 * - { "language": "en", "value": "John" }
 * - { "en": "John" }
 * - [ { "en": "John" }, { "hi": "जॉन" } ]
 */
function normalizeLocalizedFields(input: any): any {
  if (input === null || input === undefined) return input;

  if (Array.isArray(input)) {
    const langMap: Record<string, string> = {};
    let isLocalizedArray = false;

    for (const item of input) {
      if (typeof item !== "object" || item === null) continue;

      const singleEntry = Object.entries(item);
      if (singleEntry.length === 1) {
        const [lang, val] = singleEntry[0];
        if (typeof val === "string") {
          langMap[lang] = val;
          isLocalizedArray = true;
          continue;
        }
      }

      const lang = item["@language"] || item["language"];
      const val = item["@value"] || item["value"];
      if (lang && typeof val === "string") {
        langMap[lang] = val;
        isLocalizedArray = true;
      }
    }

    if (isLocalizedArray) return langMap;

    return input.map(normalizeLocalizedFields);
  }

  if (typeof input === "object") {
    if (input["@language"] && input["@value"]) {
      return { [input["@language"]]: input["@value"] };
    }

    if (input["language"] && input["value"]) {
      return { [input["language"]]: input["value"] };
    }

    const result: Record<string, any> = {};
    for (const [key, val] of Object.entries(input)) {
      result[key] = normalizeLocalizedFields(val);
    }
    return result;
  }

  return input;
}

/**
 * 🌐 Pick the best localized value from object based on language priority
 */
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
