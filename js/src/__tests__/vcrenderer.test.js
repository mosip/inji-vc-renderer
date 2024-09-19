const { VCRenderer } = require('../vcrenderer.ts');
const { fetchTemplate } = require('../utils.ts');

global.fetch = jest.fn();

describe('VCRenderer', () => {
    afterEach(() => {
        jest.resetAllMocks();
    });

    it('should return the processed SVG template', async () => {
        const mockSvgContent = '<svg>{{path/to/image}}</svg>';
        fetch.mockResolvedValueOnce({
            ok: true,
            headers: {
                get: (name) => (name === 'Content-Type' ? 'image/svg+xml' : null),
            },
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

    it('replace all valid json Path with locale', async () => {
        const mockSvgContent = '<svg>{{credentialSubject/fullName}}-{{credentialSubject/gender_eng}}</svg>';
        fetch.mockResolvedValueOnce({
            ok: true,
            headers: {
                get: (name) => (name === 'Content-Type' ? 'image/svg+xml' : null),
            },
            text: () => Promise.resolve(mockSvgContent),
        });

        const data = {
            renderMethod: [{ id: 'http://example.com/template.svg' }],
            credentialSubject: {
                fullName: "Tester",
                gender: [{"value": "Male", "language": "eng"}]
            }
        };

        const result = await VCRenderer.renderSVG(data);
        expect(result).toBe('<svg>Tester-Male</svg>');
    });

    it('should return an empty string if renderMethod is not provided', async () => {
        const data = {};
        const result = await VCRenderer.renderSVG(data);
        expect(result).toBe('');
    });

});
