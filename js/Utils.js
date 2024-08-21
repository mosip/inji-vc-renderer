
const { generateQRCode } = require('@mosip/pixelpass'); 


const QRCODE_PLACEHOLDER = "{{qrCodeImage}}"


function fetchTemplate(url) {
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

async function replaceQrCode(data, templateString) {
    try{
        const qrCode = await generateQRCode(data);
        return templateString.replace(QRCODE_PLACEHOLDER, qrCode)
    } catch(error){
        console.error("Error while Generating Qr code::",error)
        return templateString
    }
    
}

module.exports = { fetchTemplate, replaceQrCode };
