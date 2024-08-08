class DateTimeUtils {
    static formatDate = (dateString) => {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        return `${year}/${month}/${day}`;
    };

    static isValidDateTime = (dateString) => {
        const dateObject = new Date(dateString);
        return (
            dateObject instanceof Date &&
            !isNaN(dateObject) &&
            dateObject.getFullYear() >= 0 &&
            dateObject.getFullYear() <= 9999
        );
    };
}

module.exports = {DateTimeUtils};
