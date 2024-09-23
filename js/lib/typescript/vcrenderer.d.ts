interface RenderMethod {
    id: string;
}
interface Data {
    renderMethod?: RenderMethod[];
    [key: string]: any;
}
export declare class VCRenderer {
    static renderSVG(vcJsonData: Data): Promise<string>;
}
export {};
//# sourceMappingURL=vcrenderer.d.ts.map