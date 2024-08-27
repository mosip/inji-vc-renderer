import { fetchTemplate, replaceQrCode } from './utils';

interface RenderMethod {
    id: string;
}

interface Data {
    renderMethod?: RenderMethod[];
    [key: string]: any;
}

export class VCRenderer {
    static async renderSVG(data: Data): Promise<string> {
        if (!data.renderMethod) return "";

        try {
            const templateUrl = data.renderMethod[0].id;
            let templateString = await fetchTemplate(templateUrl);
            templateString = await replaceQrCode(JSON.stringify(data), templateString);
            return templateString.replace(/{{(.*?)}}/g, (match: string, key: string) => {
                key = key.replace(/^\//, '').replace(/\/$/, '');
                const keys = key.split('/');
                let value: any = data;
                keys.forEach((k: string) => {
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
