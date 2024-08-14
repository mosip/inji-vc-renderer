

function fetchTemplate(url) {
    return fetch(url)
        .then(response => {
            // Check if the response was successful
            if (!response.ok) {
                throw new Error(`Unexpected response status: ${response.status}`);
            }

            // Check Content-Type
            const contentType = response.headers.get('Content-Type');
            if (contentType !== 'image/svg+xml') {
                throw new Error(`Expected image/svg+xml but received ${contentType}`);
            }

            // Return SVG as text
            return response.text()
                .then(body => {
                    if (!body) {
                        throw new Error('Empty response body');
                    }
                    return body;
                });
        })
        .catch(error => {
            console.error('Error fetching SVG:', error);
            return ''; // Return an empty string on error
        });
}

module.exports = { fetchTemplate }; // Export the function using CommonJS syntax
