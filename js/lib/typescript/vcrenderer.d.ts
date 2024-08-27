interface RenderMethod {
    id: string;
}
interface Data {
    renderMethod?: RenderMethod[];
    [key: string]: any;
}
export declare class VCRenderer {
    static renderSVG(data: Data): Promise<string>;
}
export {};
//# sourceMappingURL=vcrenderer.d.ts.map