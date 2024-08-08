const { DateTimeUtils } = require('../DateTimeUtils');

describe('DateTimeUtils', () => {
    test('formatDate should format date strings correctly', () => {
        expect(DateTimeUtils.formatDate('2024-08-07T00:00:00Z')).toBe('2024/08/07');
        expect(DateTimeUtils.formatDate('2000-01-01')).toBe('2000/01/01');
    });

    test('isValidDateTime should return true for valid date strings', () => {
        expect(DateTimeUtils.isValidDateTime('2024-08-07T00:00:00Z')).toBe(true);
        expect(DateTimeUtils.isValidDateTime('2000-01-01')).toBe(true);
    });

    test('isValidDateTime should return false for invalid date strings', () => {
        expect(DateTimeUtils.isValidDateTime('invalid-date')).toBe(false);
        expect(DateTimeUtils.isValidDateTime('9999-12-32')).toBe(false);
        expect(DateTimeUtils.isValidDateTime('')).toBe(false);
        expect(DateTimeUtils.isValidDateTime('55555')).toBe(false);
        expect(DateTimeUtils.isValidDateTime('1234-1920')).toBe(false);
    });
});
