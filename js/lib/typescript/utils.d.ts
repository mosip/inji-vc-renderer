export declare function fetchTemplate(url: string): Promise<string>;
export declare function replaceQrCode(data: string, templateString: string): Promise<string>;
export declare function replaceBenefits(jsonObject: any, svgTemplate: string): string;
export declare function replaceAddress(jsonObject: any, svgTemplate: string): string;
export declare function replaceMultiLinePlaceholders(svgTemplate: string, dataToSplit: string, maxLength: number, placeholdersList: string[]): string;
export declare function getValueBasedOnLanguage(arrayOfObjects: any[], language: string): string;
//# sourceMappingURL=utils.d.ts.map