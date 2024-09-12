"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.VCRenderer = void 0;
var _utils = require("./utils");
class VCRenderer {
  static async renderSVG(data) {
    if (!data.renderMethod) return "";
    try {
      const templateUrl = data.renderMethod[0].id;
      let templateString = await (0, _utils.fetchTemplate)(templateUrl);
      templateString = await (0, _utils.replaceQrCode)(JSON.stringify(data), templateString);
      templateString = (0, _utils.replaceBenefits)(data, templateString);
      templateString = await (0, _utils.replaceAddress)(data, templateString);
      return templateString.replace(/{{(.*?)}}/g, (match, key) => {
        key = key.replace(/^\//, '').replace(/\/$/, '');
        const keys = key.split('/');
        let value = data;
        keys.forEach(k => {
          if (value) {
            value = value[k];
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
exports.VCRenderer = VCRenderer;
//# sourceMappingURL=vcrenderer.js.map