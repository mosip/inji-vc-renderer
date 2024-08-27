import { fetchTemplate, replaceQrCode } from './utils';
export class VCRenderer {
  static async renderSVG(data) {
    if (!data.renderMethod) return "";
    try {
      const templateUrl = data.renderMethod[0].id;
      let templateString = await fetchTemplate(templateUrl);
      templateString = await replaceQrCode(JSON.stringify(data), templateString);
      return templateString.replace(/{{(.*?)}}/g, (match, key) => {
        key = key.replace(/^\//, '').replace(/\/$/, '');
        const keys = key.split('/');
        let value = data; // Type as any for dynamic property access
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
//# sourceMappingURL=vcrenderer.js.map