import { preProcessTemplate } from './preprocessor';
import { fetchTemplate } from './utils';

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
            let svgTemplate = await fetchTemplate(templateUrl);

            svgTemplate = await preProcessTemplate(JSON.stringify(data), svgTemplate)

            return svgTemplate.replace(/{{(.*?)}}/g, (match: string, key: string) => {
                key = key.replace(/^\//, '').replace(/\/$/, '');
                const keys = key.split('/');
                let value: any = data;
                keys.forEach((k: string) => {
                    if (value) {
                                if (k.includes('_')) {
                                    const jsonPath = k.split('_')[0];
                                    const language = k.split('_')[1];
                                    value = value[jsonPath].find((g: any) => g.language === language)?.value;
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
