const { fetchTemplate, replaceAddress, replaceBenefits, replaceQrCode, replaceMultiLinePlaceholders } = require('../utils.ts');

global.fetch = jest.fn();

describe('fetchTemplate', () => {
    beforeEach(() => {
        fetch.mockClear();
    });

    it('should return SVG content when response is valid', async () => {
        const mockSvgContent = '<svg></svg>';
        fetch.mockResolvedValueOnce({
            ok: true,
            headers: {
                get: (name) => (name === 'Content-Type' ? 'image/svg+xml' : null),
            },
            text: () => Promise.resolve(mockSvgContent),
        });

        const result = await fetchTemplate('http://example.com/svg');
        expect(result).toBe(mockSvgContent);
    });

    it('should handle non-SVG content types', async () => {
        fetch.mockResolvedValueOnce({
            ok: true,
            headers: {
                get: (name) => (name === 'Content-Type' ? 'text/html' : null),
            },
            text: () => Promise.resolve('<html></html>'),
        });

        const result = await fetchTemplate('http://example.com/other');
        expect(result).toBe('');
    });

    it('should handle errors in response', async () => {
        fetch.mockResolvedValueOnce({
            ok: false,
            status: 404,
            text: () => Promise.resolve(''),
        });

        const result = await fetchTemplate('http://example.com/404');
        expect(result).toBe('');
    });

    it('should handle empty response body', async () => {
        fetch.mockResolvedValueOnce({
            ok: true,
            headers: {
                get: (name) => (name === 'Content-Type' ? 'image/svg+xml' : null),
            },
            text: () => Promise.resolve(''),
        });

        const result = await fetchTemplate('http://example.com/empty');
        expect(result).toBe('');
    });

    it('should log errors', async () => {
        console.error = jest.fn();

        fetch.mockRejectedValueOnce(new Error('Network error'));
        const result = await fetchTemplate('http://example.com/error');
        expect(result).toBe('');

        const errorCalls = console.error.mock.calls;
        console.log('Console Error Calls:', console.error.mock.calls);
        expect(errorCalls.length).toBe(1);

        expect(errorCalls[0][0]).toBe('Error fetching SVG:');
        expect(errorCalls[0][1]).toBeInstanceOf(Error);
        expect(errorCalls[0][1].message).toBe('Network error');
    });

});

describe('replaceBenefits', () => {
    it('should replace benefits placeholders with a comma-separated benefits string', () => {
      const jsonObject = {
        credentialSubject: {
          benefits: ['Benefit1', 'Benefit2', 'Benefit3', 'Benefit4', 'Benefit5', 'Benefit6'],
        },
      };
      const svgTemplate = '<svg>Benefit1,Benefit2,Benefit3,Benefit4-,Benefit5,Benefit6</svg>';
      const result = replaceBenefits(jsonObject, svgTemplate);
  
      expect(result).toMatch("<svg>Benefit1,Benefit2,Benefit3,Benefit4-,Benefit5,Benefit6</svg>");
    });
  
    it('should return the original SVG template if no benefits are provided', () => {
      const jsonObject = {
        credentialSubject: {
          benefits: [],
        },
      };
      const svgTemplate = '<svg>{BENEFITS1}</svg>{BENEFITS2}';
      const result = replaceBenefits(jsonObject, svgTemplate);
  
      expect(result).toBe(svgTemplate);
    });
  
    it('should handle errors and return the original SVG template', () => {
      const jsonObject = null;
      const svgTemplate = '<svg>{BENEFITS1}</svg>{BENEFITS2}';
      const result = replaceBenefits(jsonObject, svgTemplate);
  
      expect(result).toBe(svgTemplate);
    });
  });
  
  describe('replaceAddress', () => {
    it('should replace address placeholders with a formatted address string', () => {
      const jsonObject = {
        credentialSubject: {
          addressLine1: [{ value: '123 Main St' }],
          addressLine2: [{ value: 'Apt 4B' }],
          city: [{ value: 'Springfield' }],
          province: [{ value: 'IL' }],
          region: [{ value: 'Central' }],
          postalCode: [{ value: '62701' }],
        },
      };
      const svgTemplate = '<svg>{{fullAddress1}}</svg>';
      const result = replaceAddress(jsonObject, svgTemplate);
  
      expect(result).toMatch(/<svg>123 Main St, Apt 4B, Springfield, I<\/svg>/);
    });
  
    it('should handle cases with missing address components', () => {
      const jsonObject = {
        credentialSubject: {
          addressLine1: [{ value: '123 Main St' }],
        },
      };
      const svgTemplate = '<svg>{{fullAddress1}}-{{fullAddress2}}</svg>';
      const result = replaceAddress(jsonObject, svgTemplate);
  
      expect(result).toContain('<svg>123 Main St-{{fullAddress2}}</svg>');
    });
  
    it('should handle errors and return the original SVG template', () => {
      const jsonObject = null;
      const svgTemplate = '<svg>{ADDRESS1}</svg>{ADDRESS2}';
      const result = replaceAddress(jsonObject, svgTemplate);
  
      expect(result).toBe(svgTemplate);
    });
  });
  
  describe('replaceMultiLinePlaceholders', () => {
    it('should split and replace placeholders with data segments', () => {
      const svgTemplate = '<svg>{PLACEHOLDER1}</svg>{PLACEHOLDER2}';
      const dataToSplit = 'This is a long string that needs to be split into segments.';
      const maxLength = 20;
      const placeholdersList = ['{PLACEHOLDER1}', '{PLACEHOLDER2}'];
      const result = replaceMultiLinePlaceholders(svgTemplate, dataToSplit, maxLength, placeholdersList);
  
      expect(result).toContain('This is a long strin');
      expect(result).toContain('g that needs to be');
    });
  
    it('should handle cases where data is shorter than maxLength', () => {
      const svgTemplate = '<svg>{PLACEHOLDER1}</svg>';
      const dataToSplit = 'Short string';
      const maxLength = 20;
      const placeholdersList = ['{PLACEHOLDER1}'];
      const result = replaceMultiLinePlaceholders(svgTemplate, dataToSplit, maxLength, placeholdersList);
  
      expect(result).toContain('Short string');
    });
  
    it('should return the original SVG template if no placeholders are provided', () => {
      const svgTemplate = '<svg></svg>';
      const dataToSplit = 'Data';
      const maxLength = 20;
      const placeholdersList = [];
      const result = replaceMultiLinePlaceholders(svgTemplate, dataToSplit, maxLength, placeholdersList);
  
      expect(result).toBe(svgTemplate);
    });
  });
