const { VCRenderer } = require('../vcrenderer.ts');

global.fetch = jest.fn();

describe('VCRenderer', () => {
    afterEach(() => {
        jest.resetAllMocks();
    });

    it('should return the processed SVG template', async () => {
        const mockSvgContent = '<svg>{{credentialSubject/fullName}}</svg>';
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
                fullName: "Tester"
            },
        };

        const result = await VCRenderer.renderSVG(data);
        expect(result).toBe('<svg>Tester</svg>');
    });

    it('replace all valid json Path with locale', async () => {
        const mockSvgContent = '<svg>{{credentialSubject/fullName}}-{{credentialSubject/gender/eng}}</svg>';
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

    it("should replace locale-based fields like addressLine1 correctly", async () => {
    const mockSvgContent = "<svg>{{credentialSubject/addressLine1/eng}}</svg>";
    fetch.mockResolvedValueOnce({
      ok: true,
      headers: {
        get: (name) => (name === "Content-Type" ? "image/svg+xml" : null),
      },
      text: () => Promise.resolve(mockSvgContent),
    });

    const data = {
      renderMethod: [{ id: "http://example.com/template.svg" }],
      credentialSubject: {
        addressLine1: [
          { language: "eng", value: "TEST_ADDRESSLINE1eng" },
          { language: "tam", value: "TEST_ADDRESSLINE1tam" },
        ],
      },
    };

    const result = await VCRenderer.renderSVG(data);
    expect(result).toBe("<svg>TEST_ADDRESSLINE1eng</svg>");
  });

});
