const { fetchTemplate } = require('./Utils.js');

class VCRenderer {
    static renderSVG = async (data) => {
        if (!data.renderMethod) return ""; 

        try {
            const templateUrl = data.renderMethod[0].id;
            const templateString = await fetchTemplate(templateUrl);

            // Process the templateString as needed
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
            return ""; // Return an empty string if an exception occurs
        }
    };
}

module.exports = { VCRenderer };
