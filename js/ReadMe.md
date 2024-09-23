### InjiVcRenderer - Js Library

#### Features
- Downloads the SVG template from the renderMethod field of the VC to support VC Data Model 2.0.
- Replace the placeholders in the SVG template with actual VC Json Data.

#### Running the Example App

**Steps to run example app**
- Run `cd js`and go into js folder

- Run below command to run the example application
	`npm run example
`
- To Modify the Template

	- update "id" field with url pointing to svg template over here [sample-vc.js](example/sample-vc.js)

- To Modify the VC Data

	- update VC in [sample-vc.js](example/sample-vc.js)

Example app will print the valid SVG Image replacing the psvg template placeholders with actual VC Data.

  
### Building and Testing the Js Library

- To Build the library,

	`npm run build` which in turn runs `bob build`

- To run tests for the library,

	`npm run test`

#### API
- `renderSvg(vcJsonData: String)` - expects the Verifiable Credential as parameter and returns the replaced SVG Template.
    - `vcJsonData` - VC Downloaded in stringified format.
- This method takes entire VC data as input.
- Returns the Replaced svg template to render proper SVG Image.



#### Steps involved in SVG Template to SVG Image Conversion

- **Fetch SVG Template**
    - Extracts the svg template url from the render method
        - Downloads the SVG XML string.
- **PreProcess Credential Subject in VC**
  Preprocess SVG template for the Placeholders which needs some processing before replacing the placeholders.

    - **Update Locale Based Field for proper replacement**
            -  In SvgTemplate, the fields which requires translation should have the placeholders end with `/locale`.
                Example: {{crendetialSubject/gender/eng}}
            - Update the locale based fields to replace the svg template placeholder directly.
            - If locales are not provided, defaults it to English language.
            - Example
    
          ```
          const vcJson = {"credentialSubject" : "gender": [{"value": "Male", "language":"eng"},
          {"value": "mâle", "language":"fr"}
          ]
          //After updating the locale based fields
          const updatedVcJson = {"credentialSubject" : "gender": {"eng": "Male", "fr":"mâle"}}
        ```
    - **Update QR Code**
        - Generates the QR code using Pixelpass library and add the `qrCodeImage` field in credentialSubject

    - **Update Benefits Array Field for Multi line text**
        -  We are splitting the whole comma separate benefits string into two lines through code to accommodate in the svg template design and replacing two placeholders {{benefitsLine1}} and {{benefitsLine2}}.
        - SVG Template must have the placeholders like {{benefitsLine1}}, {{benefitsLine1}} and so on as many as the number of lines they want to split the comma separated benefits string.
        - Update the benefits value field in CredentialSubject,
        - Example

      ```
      const vcJson = {"credentialSubject" : "benefits": ["Critical Surgery", "Full Health Checkup", "Testing"]}
      
      const svgTempalte = "<svg>{{benefitsLine1}} {{benefitsLine2}}</svg>"
      
      // Above VC will be converted into below
      const updatedVcJson = {"credentialSubject" : "benefitsLine1": "Critical Surgery, Full Health Checkup, Testing}
  
      ```

     - **Update Address Fields for Multi line text**
        - Check for the address fields and create comma separated full Address String.
        - We are splitting the whole comma separate full Address string into two lines through code to accommodate in the svg template design and replacing two placeholders with locales {{fullAddress1_eng}} and {{fullAddress1_eng}}.
        - SVG Template must have the placeholders like {{fullAddress1_eng}}, {{fullAddress1_eng}} and so on as many as the number of lines they want to split the comma separated address string.
        - Update the fullAddress value field in CredentialSubject,
    - Example

      ```
      const vcJson = {      "credentialSubject": {          "addressLine1": [{"value": "No 123, Test Address line1", "language": "eng"}],          "addressLine2": [{"value": "Test Address line", "language": "eng"}],          "city": [{"value": "TestCITY", "language": "eng"}],          "province": [{"value": "TESTProvince", "language": "eng"}],      }  }
      
      const svgTemplate = "<svg>{{fullAddressLine1/eng}} {{fullAddressLine2/eng}}</svg>"
      
      // Above VC will be converted into below
      const updatedVcJson = {"credentialSubject" : "fullAddressLine1": { "eng": "No 123, Test Address line1,Test Address line, TestCITY, TESTProvince "}}
      ```

 - **Replacing placeholders with PreProcessed Vc Data**
        - When the placeholder has locale like "{{credentialSubject/gender_eng}}", Replace the placeholders with appropriate locale value.
        
        ```
        const vcJson = {      "credentialSubject": { "fullName": "Tester", "gender": [{"value": Male", "language": "eng"}]}
          
          const svgTempalte = "<svg>{{credentialSubject/fullName}} - {{credentialSubject/gender/eng}}</svg>"
          
          //result => <svg>Tester - Male</svg>
          ```

- **Returns the final replaced SVG Image**