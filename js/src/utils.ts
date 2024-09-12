import { generateQRCode } from '@mosip/pixelpass';

const QRCODE_PLACEHOLDER = "{{qrCodeImage}}";
const BENEFITS_PLACEHOLDER1 = '{{benefits1}}';
const BENEFITS_PLACEHOLDER2 = '{{benefits2}}';
const FULL_ADDRESS_PLACEHOLDER_1 = '{{fullAddress1}}';
const FULL_ADDRESS_PLACEHOLDER_2 = '{{fullAddress2}}';

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

export async function replaceQrCode(data: string, templateString: string): Promise<string> {
    try {
        const qrCode = await generateQRCode(data);
        return templateString.replace(QRCODE_PLACEHOLDER, qrCode);
    } catch (error) {
        console.error("Error while generating QR code:", error);
        return templateString;
    }
}


export function replaceBenefits(jsonObject: any, svgTemplate: string): string {
    try {
        const credentialSubject = jsonObject.credentialSubject;
        const benefitsArray = credentialSubject.benefits as string[];

        const benefitsString = benefitsArray.join(',');
        const benefitsPlaceholderList = [BENEFITS_PLACEHOLDER1, BENEFITS_PLACEHOLDER2]
        const replacedSvgWithBenefits = replaceMultiLinePlaceholders(svgTemplate,benefitsString,35, benefitsPlaceholderList)
        return replacedSvgWithBenefits;
    } catch (e) {
        console.error(e);
        return svgTemplate;
    }
}

export function replaceAddress(jsonObject: any, svgTemplate: string): string {
    try {
      const credentialSubject = jsonObject.credentialSubject;
      
      const fields = ["addressLine1", "addressLine2", "addressLine3", "city", "province", "region", "postalCode"];
      const values: string[] = [];
      fields.forEach(field => {
        if (Array.isArray(credentialSubject[field])) {
          const array = credentialSubject[field] as { value?: string }[];
          if (array.length > 0) {
            const value = array[0].value?.trim() || "";
            if (value) {
              values.push(value);
            }
          }
        }
      });
  
      const fullAddress = values.join(", ");
      const addressPlacholderList = [FULL_ADDRESS_PLACEHOLDER_1, FULL_ADDRESS_PLACEHOLDER_2];
      return replaceMultiLinePlaceholders(svgTemplate, fullAddress, 35, addressPlacholderList);
    } catch (e) {
      console.error(e);
      return svgTemplate;
    }
  }

export function replaceMultiLinePlaceholders(
    svgTemplate: string,
    dataToSplit: string,
    maxLength: number,
    placeholdersList: string[]
  ): string {
    try {
      const segments = dataToSplit.match(new RegExp(`.{1,${maxLength}}`, 'g'))?.slice(0, 2) || [];
      let replacedSvg = svgTemplate;
      placeholdersList.forEach((placeholder, index) => {
        if (index < segments.length) {
          replacedSvg = replacedSvg.replace(new RegExp(placeholder, 'i'), segments[index]);
        }
      });
  
      return replacedSvg;
    } catch (e) {
      console.error(e);
      return svgTemplate;
    }
  }
