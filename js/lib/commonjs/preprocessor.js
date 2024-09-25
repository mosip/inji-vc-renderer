"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.getPlaceholdersList = getPlaceholdersList;
exports.preProcessVcJson = preProcessVcJson;
var _constants = require("./constants");
var _pixelpass = require("@mosip/pixelpass");
async function preProcessVcJson(vcJsonString, svgTemplate) {
  const vcJsonObject = JSON.parse(vcJsonString);
  let credentialSubject = vcJsonObject.credentialSubject;
  credentialSubject = replaceFieldsWithLanguage(credentialSubject);

  // Checks for {{qrCodeImage}} for QR Code Replacement
  if (svgTemplate.includes(_constants.QRCODE_PLACEHOLDER)) {
    const qrCode = await replaceQRCode(vcJsonString);
    credentialSubject[getFieldNameFromPlaceholder(_constants.QRCODE_PLACEHOLDER)] = qrCode;
  }

  // Checks for benefits placeholders
  const benefitsPlaceholderRegex = new RegExp(_constants.BENEFITS_PLACEHOLDER_REGEX_PATTERN, 'g');
  if (benefitsPlaceholderRegex.test(svgTemplate)) {
    const benefitsPlaceholders = getPlaceholdersList(benefitsPlaceholderRegex, svgTemplate);
    const language = extractLanguageFromPlaceholder(benefitsPlaceholders[0]);
    const commaSeparatedBenefits = generateCommaSeparatedString(credentialSubject, [_constants.BENEFITS_FIELD_NAME], language);
    credentialSubject = constructObjectBasedOnCharacterLengthChunks({
      dataToSplit: commaSeparatedBenefits,
      placeholderList: benefitsPlaceholders,
      maxCharacterLength: 55
    }, credentialSubject, language);
    delete credentialSubject[_constants.BENEFITS_FIELD_NAME];
  }

  // Checks for address placeholders
  const fullAddressRegex = new RegExp(_constants.FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN, 'g');
  if (fullAddressRegex.test(svgTemplate)) {
    const addressFields = [_constants.ADDRESS_LINE_1, _constants.ADDRESS_LINE_2, _constants.ADDRESS_LINE_3, _constants.CITY, _constants.PROVINCE, _constants.POSTAL_CODE, _constants.REGION];
    const fullAddressPlaceholders = getPlaceholdersList(fullAddressRegex, svgTemplate);
    const language = extractLanguageFromPlaceholder(fullAddressPlaceholders[0]);
    const commaSeparatedAddress = generateCommaSeparatedString(credentialSubject, addressFields, language);
    credentialSubject = constructObjectBasedOnCharacterLengthChunks({
      dataToSplit: commaSeparatedAddress,
      placeholderList: fullAddressPlaceholders,
      maxCharacterLength: 55
    }, credentialSubject, language);
    addressFields.forEach(fieldName => delete credentialSubject[fieldName]);
  }
  vcJsonObject.credentialSubject = credentialSubject;
  return vcJsonObject;
}
function replaceFieldsWithLanguage(jsonObject) {
  const keys = Object.keys(jsonObject);
  for (const key of keys) {
    const value = jsonObject[key];
    if (Array.isArray(value)) {
      const languageMap = {};
      let hasLanguage = false;
      value.forEach(item => {
        if (typeof item === 'object' && item.language) {
          hasLanguage = true;
          languageMap[item.language] = item.value;
        }
      });
      if (hasLanguage) {
        jsonObject[key] = languageMap;
      }
    } else if (typeof value === 'object') {
      replaceFieldsWithLanguage(value);
    }
  }
  return jsonObject;
}
function getFieldNameFromPlaceholder(placeholder) {
  const regex = new RegExp(GET_PLACEHOLDER_REGEX);
  const match = regex.exec(placeholder);
  const enclosedValue = match === null || match === void 0 ? void 0 : match[1];
  return (enclosedValue === null || enclosedValue === void 0 ? void 0 : enclosedValue.split('/').pop()) || '';
}
function extractLanguageFromPlaceholder(placeholder) {
  const regex = new RegExp(GET_LANGUAGE_FORM_PLACEHOLDER_REGEX);
  const match = regex.exec(placeholder);
  return (match === null || match === void 0 ? void 0 : match[1]) || '';
}
function getPlaceholdersList(placeholderRegexPattern, svgTemplate) {
  const placeholders = [];
  const globalPattern = new RegExp(placeholderRegexPattern.source, 'g');
  const matches = svgTemplate.matchAll(globalPattern);
  for (const match of matches) {
    placeholders.push(match[0]);
  }
  return placeholders;
}
async function replaceQRCode(vcJson) {
  try {
    const qrCode = await (0, _pixelpass.generateQRCode)(vcJson);
    return qrCode;
  } catch (e) {
    console.error(e);
    return '';
  }
}
function generateCommaSeparatedString(jsonObject, fieldsToBeCombined, language) {
  return fieldsToBeCombined.flatMap(field => {
    const fieldValue = jsonObject[field];
    if (Array.isArray(fieldValue)) {
      return fieldValue.map(item => typeof item === 'string' ? item : item.value).filter(Boolean);
    } else if (typeof fieldValue === 'object') {
      return fieldValue[language] ? [fieldValue[language]] : [];
    }
    return [];
  }).join(', ');
}
function constructObjectBasedOnCharacterLengthChunks(multiLineProperties, jsonObject, language) {
  const segments = multiLineProperties.dataToSplit.match(new RegExp(`.{1,${multiLineProperties.maxCharacterLength}}`, 'g')) || [];
  multiLineProperties.placeholderList.forEach((placeholder, index) => {
    if (index < segments.length) {
      jsonObject[getFieldNameFromPlaceholder(placeholder)] = language ? {
        [language]: segments[index]
      } : segments[index];
    }
  });
  return jsonObject;
}
const GET_PLACEHOLDER_REGEX = /{{credentialSubject\/([^/]+)(?:\/[^}]+)?}}/;
const GET_LANGUAGE_FORM_PLACEHOLDER_REGEX = /credentialSubject\/[^/]+\/(\w+)/;
//# sourceMappingURL=preprocessor.js.map