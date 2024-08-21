const { fetchTemplate } = require('../Utils');
global.fetch = jest.fn();

describe('fetchTemplate', () => {
    beforeEach(() => {
        fetch.mockClear();
    });

    it('should return SVG content when response is valid', async () => {
        const mockSvgContent = '<svg></svg>';
        fetch.mockResolvedValueOnce({
            ok: true,
            headers: new Headers({ 'Content-Type': 'image/svg+xml' }),
            text: () => Promise.resolve(mockSvgContent),
        });

        const result = await fetchTemplate('http://example.com/svg');
        expect(result).toBe(mockSvgContent);
    });

    it('should handle non-SVG content types', async () => {
        fetch.mockResolvedValueOnce({
            ok: true,
            headers: new Headers({ 'Content-Type': 'text/html' }),
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
            headers: new Headers({ 'Content-Type': 'image/svg+xml' }),
            text: () => Promise.resolve(''),
        });

        const result = await fetchTemplate('http://example.com/empty');
        expect(result).toBe('');
    });

    it('should log errors', async () => {
        console.error = jest.fn(); // Mock console.error

        fetch.mockRejectedValueOnce(new Error('Network error'));

        const result = await fetchTemplate('http://example.com/error');
        expect(result).toBe('');
        expect(console.error).toHaveBeenCalledWith('Error fetching SVG:', expect.any(Error));
    });
});
