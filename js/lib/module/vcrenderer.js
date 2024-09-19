import { preProcessTemplate } from './preprocessor';
import { fetchTemplate } from './utils';
export class VCRenderer {
  static async renderSVG(data) {
    if (!data.renderMethod) return "";
    try {
      const templateUrl = data.renderMethod[0].id;
      let svgTemplate = await fetchTemplate(templateUrl);
      svgTemplate = await preProcessTemplate(JSON.stringify(data), svgTemplate);
      return svgTemplate.replace(/{{(.*?)}}/g, (match, key) => {
        key = key.replace(/^\//, '').replace(/\/$/, '');
        const keys = key.split('/');
        let value = data;
        keys.forEach(k => {
          if (value) {
            if (k.includes('_')) {
              var _value$jsonPath$find;
              const jsonPath = k.split('_')[0];
              const language = k.split('_')[1];
              value = (_value$jsonPath$find = value[jsonPath].find(g => g.language === language)) === null || _value$jsonPath$find === void 0 ? void 0 : _value$jsonPath$find.value;
            } else {
              value = value[k];
            }
          }
        });
        return value !== undefined ? String(value) : '';
      });
    } catch (error) {
      console.error('Failed to generate the SVG image:', error);
      return "";
    }
  }
}
//# sourceMappingURL=vcrenderer.js.map