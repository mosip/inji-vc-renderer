const { BENEFITS_FIELD_NAME, BENEFITS_PLACEHOLDER_REGEX_PATTERN } = require('../constants.ts');
const {transformArrayFieldsIntoMultiline, transformAddressFieldsIntoMultiline, wrapBasedOnCharacterLength, getPlaceholdersList, preProcessTemplate} = require('../preprocessor.ts');

describe('preProcessTemplate', () => {
  it('call replaceBenefits', async () => {
    const svgTemplate = "<svg>{{benefits1}}</svg>"
    const vcJsonString = `{
      "credentialSubject": {
        "benefits": ["Critical Surgery", "Full Body Checkup"]
      }
      
    }`;
    const result = await preProcessTemplate(vcJsonString, svgTemplate);
    expect(result).toMatch("<svg>Critical Surgery,Full Body Checkup</svg>");
  });

  it('call replaceAddress', async () => {
    const svgTemplate = "<svg>{{fullAddress1_eng}}</svg>"
    const vcJsonString = `{
      "credentialSubject": {
          "addressLine1": [
              {
                  "value": "Test Address",
                  "language": "eng"
              }
          ]
      }
  }`;
    const result = await preProcessTemplate(vcJsonString, svgTemplate);
    expect(result).toMatch("<svg>Test Address</svg>");
  });

  it('no valid preprocess placeholders', async () => {
    const svgTemplate = "<svg>{{tester}}</svg>"
    const vcJsonString = `{
      "credentialSubject": {
          "addressLine1": [
              {
                  "value": "Test Address",
                  "language": "eng"
              }
          ]
      }
  }`;
    const result = await preProcessTemplate(vcJsonString, svgTemplate);
    expect(result).toMatch(svgTemplate);
  });

  it('locale not available in data', async () => {
    const svgTemplate = "<svg>{{fullAddress1_tam}}</svg>"
    const vcJsonString = `{
      "credentialSubject": {
          "addressLine1": [
              {
                  "value": "Test Address",
                  "language": "eng"
              }
          ]
      }
  }`;
    const result = await preProcessTemplate(vcJsonString, svgTemplate);
    expect(result).toMatch(svgTemplate);
  });

});

describe('replaceBenefits', () => {
    it('should replace benefits placeholders with a comma-separated benefits string', () => {


      const vcJsonString = `{
        "credentialSubject": {
          "benefits": ["Benefit1", "Benefit2", "Benefit3", "Benefit4", "Benefit5", "Benefit6"]
        }
        
      }`;

      const vcJsonObject = JSON.parse(vcJsonString)

      const svgTemplate = '<svg>{{benefits1}}{{benefits2}}</svg>';
      const result = transformArrayFieldsIntoMultiline(vcJsonObject, 
                svgTemplate, 
                { placeholders: ["{{benefits1}}", "{{benefits2}}"], maxCharacterLength: 9, fieldName: BENEFITS_FIELD_NAME });
  
      expect(result).toMatch("<svg>Benefit1,Benefit2,</svg>");
    });

    it('should return the original SVG template if empty benefits array provided', () => {
      const jsonObject = {
        credentialSubject: {
          benefits: [],
        },
      };
      const svgTemplate = '<svg>{{benefits1}}{{benefits2}}</svg>';
      const result = transformArrayFieldsIntoMultiline(jsonObject, 
            svgTemplate,
            { placeholders: ["{{benefits1}}", "{{benefits2}}"], maxCharacterLength: 9, fieldName: BENEFITS_FIELD_NAME });
  
      expect(result).toBe(svgTemplate);
    });
  
    it('should return the original SVG template if no benefits fields available', () => {
      const jsonObject = {
        credentialSubject: {
          otherFields: [],
        },
      };
      const svgTemplate = '<svg>{{benefits1}}{{benefits2}}</svg>';
      const result = transformArrayFieldsIntoMultiline(jsonObject, 
            svgTemplate,
            { placeholders: ["{{benefits1}}", "{{benefits2}}"], maxCharacterLength: 9, fieldName: BENEFITS_FIELD_NAME });
  
      expect(result).toBe(svgTemplate);
    });
  });
  
describe('replaceAddress', () => {
it('should replace address placeholders with a formatted address string', () => {
  const jsonObject = {
    credentialSubject: {
      addressLine1: [{ value: '123 Main St', language: 'eng' }],
      addressLine2: [{ value: 'Apt 4B',  language: 'eng' }],
      city: [{ value: 'Springfield',  language: 'eng' }],
      province: [{ value: 'IL',  language: 'eng'}],
      region: [{ value: 'Central',  language: 'eng'}],
      postalCode: [{ value: '62701',  language: 'eng'}],
    },
  };
  const svgTemplate = '<svg>{{fullAddress1_eng}}{{fullAddress2_eng}}</svg>';
  const result = transformAddressFieldsIntoMultiline(jsonObject, 
      svgTemplate, 
      { placeholders: ["{{fullAddress1_eng}}","{{fullAddress2_eng}}"], maxCharacterLength: 10});
      

  expect(result).toMatch(/<svg>123 Main St, Apt 4B,<\/svg>/);
});

it('should replace address placeholders with not matching locale', () => {
  const jsonObject = {
    credentialSubject: {
      addressLine1: [{ value: '123 Main St', language: 'eng' }],
      addressLine2: [{ value: 'Apt 4B',  language: 'eng' }],
      city: [{ value: 'Springfield',  language: 'eng' }],
      province: [{ value: 'IL',  language: 'eng'}],
      region: [{ value: 'Central',  language: 'eng'}],
      postalCode: [{ value: '62701',  language: 'eng'}],
    },
  };
  const svgTemplate = '<svg>{{fullAddress1_tam}}{{fullAddress2_tam}}</svg>';
  const result = transformAddressFieldsIntoMultiline(jsonObject, 
      svgTemplate, 
      { placeholders: ["{{fullAddress1_tam}}","{{fullAddress2_tam}}"], maxCharacterLength: 10});
      

  expect(result).toMatch(svgTemplate);
});

it('should not replace address placeholders with not matching locale', () => {
  const jsonObject = {
    credentialSubject: {
      addressLine1: [{ value: '123 Main St', language: 'eng' }],
      addressLine2: [{ value: 'Apt 4B',  language: 'eng' }],
      city: [{ value: 'Springfield',  language: 'eng' }],
      province: [{ value: 'IL',  language: 'eng'}],
      region: [{ value: 'Central',  language: 'eng'}],
      postalCode: [{ value: '62701',  language: 'eng'}],
    },
  };
  const svgTemplate = '<svg>{{fullAddress1_tam}}{{fullAddress2_tam}}</svg>';
  const result = transformAddressFieldsIntoMultiline(jsonObject, 
      svgTemplate, 
      { placeholders: ["{{fullAddress1_tam}}","{{fullAddress2_tam}}"], maxCharacterLength: 10});
      

  expect(result).toMatch(svgTemplate);
});

it('should not replace address placeholders when no address fields found', () => {
  const jsonObject = {
    credentialSubject: {
    },
  };
  const svgTemplate = '<svg>{{fullAddress1_eng}}{{fullAddress2_eng}}</svg>';
  const result = transformAddressFieldsIntoMultiline(jsonObject, 
      svgTemplate, 
      { placeholders: ["{{fullAddress1_eng}}","{{fullAddress2_eng}}"], maxCharacterLength: 10});
      

  expect(result).toMatch(svgTemplate);
});


});


describe("wrapBasedOnCharacterLength", () => {
  it('should return valid character chucks', () => {
    const svgTemplate = '<svg>First Line: {{benefits1}},Second Line: {{benefits2}}</svg>';
    const placeholders = ["{{benefits1}}", "{{benefits2}}"]
    const dataToSplit = "Crtical Surgery, Full health checkup"
    const result = wrapBasedOnCharacterLength(svgTemplate, dataToSplit, 5, placeholders)

    expect(result).toMatch("<svg>First Line: Crtic,Second Line: al Su</svg>")
  })

  it('with no data to split', () => {
    const svgTemplate = '<svg>First Line: {{benefits1}},Second Line: {{benefits2}}</svg>';
    const placeholders = ["{{benefits1}}", "{{benefits2}}"]
    const dataToSplit = ""
    const result = wrapBasedOnCharacterLength(svgTemplate, dataToSplit, 5, placeholders)

    expect(result).toMatch(svgTemplate)
  })


});

describe("getPlaceholdersList", () => {
  it('valid placeholders list', () => {
    const svgTemplate = '<svg>First Line: {{benefits1}},Second Line: {{benefits2}}</svg>';
    const result = getPlaceholdersList(new RegExp(BENEFITS_PLACEHOLDER_REGEX_PATTERN), svgTemplate);

    expect(result).toEqual(["{{benefits1}}", "{{benefits2}}"]);
  });
  it('invalid placeholders list', () => {
    const svgTemplate = '<svg>First Line: {{someOther1}},Second Line: {{someOther2}}</svg>';
    const result = getPlaceholdersList(new RegExp(BENEFITS_PLACEHOLDER_REGEX_PATTERN), svgTemplate);
    expect(result).toEqual([]);
  });
});

