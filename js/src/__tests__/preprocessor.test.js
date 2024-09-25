
const {preProcessVcJson } = require('../preprocessor.ts');

describe('preProcessTemplate', () => {
  
  it('test localeBasedFields', async () => {
    const vcJsonString = `{
      "credentialSubject": {
          "gender": [
              {
                  "language": "eng",
                  "value": "English Male"
              },
              {
                  "language": "tam",
                  "value": "Tamil Male"
              }
          ]
      }
    }`;
    
    const svgTemplate = "{{credentialSubject/gender/eng}}";

    const expected = {
      "credentialSubject": {
          "gender": {
              "eng": "English Male",
              "tam": "Tamil Male"
          }
      }
    };

    const result = await preProcessVcJson(vcJsonString, svgTemplate);
    expect(result).toEqual(expected);
  });

  it('test replaceAddress', async () => {
    const vcJsonString = `{
      "credentialSubject": {
          "addressLine1": [
              {
                  "language": "eng",
                  "value": "Address Line 1"
              },
              {
                  "language": "fr",
                  "value": "Address Line1 French"
              }
          ],
          "city": [
              {
                  "language": "eng",
                  "value": "City"
              },
              {
                  "language": "fr",
                  "value": "City French"
              }
          ]
      }
  }`
  const svgTemplate = "{{credentialSubject/fullAddressLine1/eng}}"

  const expected = {
      "credentialSubject": {
          "fullAddressLine1": {"eng":"Address Line 1, City"}
      }
  }

  const result = await preProcessVcJson(vcJsonString, svgTemplate)
    expect(result).toEqual(expected);
  });

  it('test replaceAddress without address field object', async () => {
    const vcJsonString = `{
      "credentialSubject": {
      }
  }`
  const svgTemplate = "{{credentialSubject/fullAddressLine1/eng}}"

  const expected = {
      "credentialSubject": {
          
      }
  }

  const result = await preProcessVcJson(vcJsonString, svgTemplate)
    expect(result).toEqual(expected);
  });

  it('test replaceBenefits', async () => {
   
    const vcJsonString = `{
      "credentialSubject": {
          "benefits": [ "Benefits one, Benefits two"
          ]
      }
    }`
    const svgTemplate = "{{credentialSubject/benefitsLine1}}"

    const expected = {
        "credentialSubject": {
            "benefitsLine1":"Benefits one, Benefits two"
        }
    }

    const result = await preProcessVcJson(vcJsonString, svgTemplate)
    expect(result).toEqual(expected);
  });

});