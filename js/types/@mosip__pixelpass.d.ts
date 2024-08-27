declare module '@mosip/pixelpass' {
    export function generateQRCode(data: string): Promise<string>;
}
