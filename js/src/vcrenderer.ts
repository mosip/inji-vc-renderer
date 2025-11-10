import { preProcessVcJson } from "./preprocessor";
import { fetchTemplate } from "./utils";
import { DEFAULT_ENG } from "./constants";

interface RenderMethod {
  renderSuite: string;
  template: {
    id: string;
    mediaType: string;
  };
  type: string;
}

interface Data {
  renderMethod?: RenderMethod[];
  [key: string]: any;
}

const PLACEHOLDER_REGEX_PATTERN = /{{(.*?)}}/g;

/**
 * 🌐 Extracts value from VC JSON by key (supports nested keys and multilingual values)
 */
function getValueFromData(
  key: string,
  jsonObject: any,
  language: string = DEFAULT_ENG,
  defaultLanguage: string = DEFAULT_ENG
): any {
  if (!key || !jsonObject) return null;
  key = key.replace(/^\/+/, "");
  const keys = key.split("/");
  let currentValue: any = jsonObject;

  for (const k of keys) {
    if (currentValue && typeof currentValue === "object") {
      if (Array.isArray(currentValue)) {
        const index = parseInt(k, 10);
        currentValue = !isNaN(index) ? currentValue[index] : null;
      } else {
        currentValue = currentValue[k];
      }
    } else {
      return null;
    }
  }

  // 🧠 Handle multilingual objects like { en: "Farmer", hi: "किसान" }
  if (typeof currentValue === "object" && currentValue !== null) {
    if (language && currentValue[language]) return currentValue[language];
    if (defaultLanguage && currentValue[defaultLanguage])
      return currentValue[defaultLanguage];
    if (currentValue[DEFAULT_ENG]) return currentValue[DEFAULT_ENG];

    // fallback: first available language value
    const firstLangValue = Object.values(currentValue)[0];
    if (firstLangValue) return firstLangValue;
  }

  return currentValue ?? null;
}

/**
 * 🖼️ VCRenderer - replaces SVG placeholders with VC JSON values.
 */
export class VCRenderer {
  static async renderSVG(
    vcJsonData: Data,
    currentLanguage: string = DEFAULT_ENG,
    defaultLanguage: string = DEFAULT_ENG
  ): Promise<string> {
    if (!vcJsonData.renderMethod) return "";

    try {
      const templateUrl = vcJsonData.renderMethod[0].template?.id;
      if (!templateUrl) return "";

      let svgTemplate = await fetchTemplate(templateUrl);
      vcJsonData = await preProcessVcJson(JSON.stringify(vcJsonData), svgTemplate, currentLanguage, defaultLanguage);

      return svgTemplate.replace(PLACEHOLDER_REGEX_PATTERN, (match: string, key: string) => {
          key = key.trim().replace(/^\/+/, "");
          const value = getValueFromData(key, vcJsonData, currentLanguage, defaultLanguage);
          return value !== undefined && value !== null ? String(value) : "";
        });
    } catch (error) {
      console.error("Failed to generate the SVG image:", error);
      return "";
    }
  }
}
