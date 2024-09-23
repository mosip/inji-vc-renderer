import { preProcessVcJson } from './preprocessor';
import { fetchTemplate } from './utils';
const PLACEHOLDER_REGEX_PATTERN = /{{(.*?)}}/g;
const DEFAULT_ENG = "eng";
function getValueFromData(key, jsonObject, isDefaultLanguageHandle = false) {
  const keys = key.split('/');
  let currentValue = jsonObject;
  for (const k of keys) {
    if (typeof currentValue === 'object' && currentValue !== null) {
      if (Array.isArray(currentValue)) {
        const index = parseInt(k, 10);
        currentValue = index >= 0 && index < currentValue.length ? currentValue[index] : null;
      } else {
        currentValue = currentValue[k];
      }
    } else {
      return null;
    }
  }

  // Setting Default Language to English
  if (typeof currentValue === 'object' && currentValue !== null) {
    return currentValue[DEFAULT_ENG] || null;
  }
  if (currentValue == null && keys.length && !isDefaultLanguageHandle) {
    const updatedKey = keys.slice(0, -1).join('/') + `/${DEFAULT_ENG}`;
    return getValueFromData(updatedKey, jsonObject, true);
  }
  return currentValue;
}
export class VCRenderer {
  static async renderSVG(vcJsonData) {
    if (!vcJsonData.renderMethod) return "";
    try {
      const templateUrl = vcJsonData.renderMethod[0].id;
      let svgTemplate = await fetchTemplate(templateUrl);
      vcJsonData = await preProcessVcJson(JSON.stringify(vcJsonData), svgTemplate);
      return svgTemplate.replace(PLACEHOLDER_REGEX_PATTERN, (match, key) => {
        key = key.trim();
        const value = getValueFromData(key, vcJsonData);
        return value !== undefined ? String(value) : '';
      });
    } catch (error) {
      console.error('Failed to generate the SVG image:', error);
      return "";
    }
  }
}
//# sourceMappingURL=vcrenderer.js.map