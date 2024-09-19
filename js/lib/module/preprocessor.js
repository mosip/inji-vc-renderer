import { BENEFITS_PLACEHOLDER_REGEX_PATTERN, QRCODE_PLACEHOLDER, BENEFITS_FIELD_NAME, FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN, ADDRESS_LINE_1, ADDRESS_LINE_2, REGION, POSTAL_CODE, PROVINCE, CITY, ADDRESS_LINE_3 } from "./constants";
import { generateQRCode } from '@mosip/pixelpass';
import { getValueBasedOnLanguage } from "./utils";
export async function preProcessTemplate(vcJsonString, svgTemplate) {
  let preProcessedTemplate = svgTemplate;

  // Checks for {{qrCodeImage}} for QR Code Replacement
  if (preProcessedTemplate.includes(QRCODE_PLACEHOLDER)) {
    preProcessedTemplate = await replaceQrCode(vcJsonString, preProcessedTemplate);
  }

  // Checks for {{benefits1}} or {{benefits2}} for Benefits Replacement
  const benefitsPlaceholderRegexPattern = new RegExp(BENEFITS_PLACEHOLDER_REGEX_PATTERN);
  if (benefitsPlaceholderRegexPattern.test(preProcessedTemplate)) {
    const benefitsPlaceholders = getPlaceholdersList(benefitsPlaceholderRegexPattern, preProcessedTemplate);
    preProcessedTemplate = transformArrayFieldsIntoMultiline(JSON.parse(vcJsonString), preProcessedTemplate, {
      placeholders: benefitsPlaceholders,
      maxCharacterLength: 55,
      fieldName: BENEFITS_FIELD_NAME
    });
  }

  // Checks for {{benefits1}} or {{benefits2}} for Benefits Replacement
  const fullAddressPlaceholderRegexPattern = new RegExp(FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN);
  if (fullAddressPlaceholderRegexPattern.test(preProcessedTemplate)) {
    const fullAddressPlaceholders = getPlaceholdersList(fullAddressPlaceholderRegexPattern, preProcessedTemplate);
    preProcessedTemplate = transformAddressFieldsIntoMultiline(JSON.parse(vcJsonString), preProcessedTemplate, {
      placeholders: fullAddressPlaceholders,
      maxCharacterLength: 55
    });
  }
  return preProcessedTemplate;
}
export function getPlaceholdersList(placeholderRegexPattern, svgTemplate) {
  const placeholders = [];
  const globalPattern = new RegExp(placeholderRegexPattern.source, 'g');
  const matches = svgTemplate.matchAll(globalPattern);
  for (const match of matches) {
    placeholders.push(match[0]);
  }
  return placeholders;
}
export async function replaceQrCode(data, templateString) {
  try {
    const qrCode = await generateQRCode(data);
    return templateString.replace(QRCODE_PLACEHOLDER, qrCode);
  } catch (error) {
    console.error("Error while generating QR code:", error);
    return templateString;
  }
}
export function transformArrayFieldsIntoMultiline(jsonObject, svgTemplate, multiLineProperties) {
  try {
    const credentialSubject = jsonObject.credentialSubject;
    const fieldName = multiLineProperties.fieldName;
    if (!fieldName) {
      return svgTemplate;
    }
    const fieldArray = credentialSubject[fieldName];
    const commaSeparatedFieldElements = fieldArray.join(',');
    const replacedSvgWithMultiLineArrayElements = wrapBasedOnCharacterLength(svgTemplate, commaSeparatedFieldElements, multiLineProperties.maxCharacterLength, multiLineProperties.placeholders);
    return replacedSvgWithMultiLineArrayElements;
  } catch (e) {
    console.error(e);
    return svgTemplate;
  }
}
function transformAddressFieldsIntoMultiline(jsonObject, svgTemplate, multiLineProperties) {
  try {
    const credentialSubject = jsonObject.credentialSubject || {};
    const fields = [ADDRESS_LINE_1, ADDRESS_LINE_2, ADDRESS_LINE_3, CITY, PROVINCE, POSTAL_CODE, REGION];
    const values = [];
    const languageRegex = /{{fullAddress1_(\w+)}}/;
    const languageMatch = svgTemplate.match(languageRegex);
    const language = languageMatch ? languageMatch[1] : '';
    fields.forEach(field => {
      if (Array.isArray(credentialSubject[field])) {
        const array = credentialSubject[field];
        if (array.length > 0) {
          const value = getValueBasedOnLanguage(array, language || '');
          if (value) {
            values.push(value);
          }
        }
      }
    });
    const fullAddress = values.join(', ');
    return wrapBasedOnCharacterLength(svgTemplate, fullAddress, multiLineProperties.maxCharacterLength, multiLineProperties.placeholders);
  } catch (e) {
    console.error(e);
    return svgTemplate;
  }
}
export function wrapBasedOnCharacterLength(svgTemplate, dataToSplit, maxLength, placeholdersList) {
  try {
    var _dataToSplit$match;
    const segments = ((_dataToSplit$match = dataToSplit.match(new RegExp(`.{1,${maxLength}}`, 'g'))) === null || _dataToSplit$match === void 0 ? void 0 : _dataToSplit$match.slice(0, 2)) || [];
    let replacedSvg = svgTemplate;
    placeholdersList.forEach((placeholder, index) => {
      if (index < segments.length) {
        replacedSvg = replacedSvg.replace(new RegExp(placeholder, 'i'), segments[index]);
      }
    });
    return replacedSvg;
  } catch (e) {
    console.error(e);
    return svgTemplate;
  }
}
//# sourceMappingURL=preprocessor.js.map