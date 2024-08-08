const { VCRenderer } = require('../VCRenderer');


global.fetch = jest.fn(() =>
    Promise.resolve({
        text: () => Promise.resolve('<svg>{{credentialSubject/name}}-{{credentialSubject/policyNumber}}</svg>')
    })
);

describe('VCRenderer', () => {
    test('renderSVG should correctly replace placeholders with data', async () => {
        const data = {
            renderMethod: [{ id: 'http://example.com/template.svg' }],
            credentialSubject: {
                "name": "John",
                "policyNumber": "123454"
            }
        };

        const result = await VCRenderer.renderSVG(data);
        expect(result).toBe('<svg>John-123454</svg>');
    });

    test('renderSVG should handle missing renderMethod gracefully', async () => {
        const data = {
            date: '2024-08-07T00:00:00Z'
        };

        const result = await VCRenderer.renderSVG(data);
        expect(result).toBe('');
    });

    test('renderSVG should handle missing data values', async () => {
        const data = {
            renderMethod: [{ id: 'http://example.com/template.svg' }],
            nonexistent: '2024-08-07T00:00:00Z'
        };

        const result = await VCRenderer.renderSVG(data);
        expect(result).toBe('<svg>-</svg>');
    });
});
