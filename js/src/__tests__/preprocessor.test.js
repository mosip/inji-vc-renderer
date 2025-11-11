const { preProcessVcJson } = require('../preprocessor.ts');

describe('preProcessTemplate', () => {
  
  it('should normalize locale-based fields correctly', async () => {
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

  it('should handle multilingual properties with mixed locales', async () => {
    const vcJsonString = `{
      "credentialSubject": {
          "name": [
              { "language": "eng", "value": "John Doe" },
              { "language": "hin", "value": "जॉन डो" }
          ]
      }
    }`;

    const svgTemplate = "{{credentialSubject/name/eng}}";

    const expected = {
      "credentialSubject": {
          "name": {
              "eng": "John Doe",
              "hin": "जॉन डो"
          }
      }
    };

    const result = await preProcessVcJson(vcJsonString, svgTemplate);
    expect(result).toEqual(expected);
  });

  it('should handle non-localized flat fields correctly', async () => {
    const vcJsonString = `{
      "credentialSubject": {
          "id": "12345",
          "age": 30
      }
    }`;

    const svgTemplate = "{{credentialSubject/id}}";

    const expected = {
      "credentialSubject": {
          "id": "12345",
          "age": 30
      }
    };

    const result = await preProcessVcJson(vcJsonString, svgTemplate);
    expect(result).toEqual(expected);
  });

  it('should handle empty credentialSubject gracefully', async () => {
    const vcJsonString = `{
      "credentialSubject": {}
    }`;

    const svgTemplate = "{{credentialSubject/anyPlaceholder}}";

    const expected = {
      "credentialSubject": {}
    };

    const result = await preProcessVcJson(vcJsonString, svgTemplate);
    expect(result).toEqual(expected);
  });

  it('should normalize addressLine1 correctly', async () => {
    const vcJsonString = `{
      "credentialSubject": {
          "addressLine1": [
              { "language": "eng", "value": "TEST_ADDRESSLINE1eng" },
              { "language": "tam", "value": "TEST_ADDRESSLINE1tam" }
          ]
      }
    }`;

    const svgTemplate = "{{credentialSubject/addressLine1/eng}}";

    const expected = {
      "credentialSubject": {
          "addressLine1": {
              "eng": "TEST_ADDRESSLINE1eng",
              "tam": "TEST_ADDRESSLINE1tam"
          }
      }
    };

    const result = await preProcessVcJson(vcJsonString, svgTemplate);
    expect(result).toEqual(expected);
  });

});
