const { fetchTemplate, replaceQrCode } = require('../src/utils.js'); 

class VCRenderer {
    static renderSVG = async (data) => {
        if (!data.renderMethod) return "";

        try {
            const templateUrl = data.renderMethod[0].id;
            var templateString = await fetchTemplate(templateUrl);
           templateString = await replaceQrCode(JSON.stringify(data), templateString)
        
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
    };
}

module.exports = { VCRenderer };
