export declare function preProcessTemplate(vcJsonString: string, svgTemplate: string): Promise<string>;
export declare function getPlaceholdersList(placeholderRegexPattern: RegExp, svgTemplate: string): string[];
export declare function replaceQrCode(data: string, templateString: string): Promise<string>;
export declare function transformArrayFieldsIntoMultiline(jsonObject: any, svgTemplate: string, multiLineProperties: MultiLineProperties): string;
export declare function wrapBasedOnCharacterLength(svgTemplate: string, dataToSplit: string, maxLength: number, placeholdersList: string[]): string;
interface MultiLineProperties {
    placeholders: string[];
    maxCharacterLength: number;
    fieldName?: string;
}
export {};
//# sourceMappingURL=preprocessor.d.ts.map