const { VCRenderer } = require('../VCRenderer');
const { fetchTemplate } = require('../Utils');

global.fetch = jest.fn();

describe('VCRenderer', () => {
    afterEach(() => {
        jest.resetAllMocks();
    });

    it('should return the processed SVG template', async () => {
        const mockSvgContent = '<svg>{{path/to/image}}</svg>';
        fetch.mockResolvedValueOnce({
            ok: true,
            headers: new Headers({ 'Content-Type': 'image/svg+xml' }),
            text: () => Promise.resolve(mockSvgContent),
        });

        const data = {
            renderMethod: [{ id: 'http://example.com/template.svg' }],
            path: {
                to: {
                    image: 'Test Image',
                },
            },
        };

        const result = await VCRenderer.renderSVG(data);
        expect(result).toBe('<svg>Test Image</svg>');
    });

    it('should return an empty string if renderMethod is not provided', async () => {
        const data = {};
        const result = await VCRenderer.renderSVG(data);
        expect(result).toBe('');
    });

});
