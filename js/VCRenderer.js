const {DateTimeUtils} = require("./DateTimeUtils");
class VCRenderer {
    static renderSVG = async(data) => {
        if(!data.renderMethod) return "";
        const templateUrl = data.renderMethod[0].id;
        const response = await fetch(templateUrl, {
            method: "GET",
            headers: {
                "Content-Type": "image/svg+xml"
            }
        });
        const templateString = await response.text();
        return templateString.replace(/{{(.*?)}}/g, (match, key) => {
            key = key.replace(/^\//, '').replace(/\/$/, '');
            const keys = key.split('/');
            let value = data;
            keys.forEach(k => {
                if (value) {
                    value = value[k];
                    if (DateTimeUtils.isValidDateTime(value)) {
                        value = DateTimeUtils.formatDate(value);
                    }
                }
            });
            return value !== undefined ? String(value) : '';
        });
    };
}
module.exports = {VCRenderer};
