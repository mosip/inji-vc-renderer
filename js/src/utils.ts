export function fetchTemplate(url: string): Promise<string> {
    return fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Unexpected response status: ${response.status}`);
            }
            const contentType = response.headers.get('Content-Type');
            if (contentType !== 'image/svg+xml') {
                throw new Error(`Expected image/svg+xml but received ${contentType}`);
            }
            return response.text().then(body => {
                if (!body) {
                    throw new Error('Empty response body');
                }
                return body;
            });
        })
        .catch(error => {
            console.error('Error fetching SVG:', error);
            return '';
        });
}
