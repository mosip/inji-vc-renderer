### InjiVcRenderer - Kotlin Library

#### Features
- Downloads the SVG template from the renderMethod field of the VC to support VC Data Model 2.0.
- Replace the placeholders in the SVG template with actual VC Json Data.
- Generates aar and jar from the library .

#### Build
- Modules in the Kotlin Project
    1. example-app
        - Application that Uses **injivcrenderer** library project to print Updated SVG Template.
        - Run using  `./gradlew :app:build`
2. injivcrenderer
    - Library to replace the placeholders in the Svg Template received from the renderMethod with the actual Verifable Credential
    - Run using `./gradlew :injivcrenderer:build` to generate the aar
    - Gradle task is registered to generate jar by running the command `./gradlew jarRelease` which creates jar in the `build/libs` folder
    - Run Tests using `./gradlew testDebugUnitTest` or `./gradlew testReleaseUnitTest` based on the build type.
#### API
- `renderSvg(vcJsonData: String)` - expects the Verifiable Credential as parameter and returns the replaced SVG Template.
    - `vcJsonData` - VC Downloaded in stringified format.
- This method takes entire VC data as input.
- Returns the Replaced svg template to render proper SVG Image.


#### Steps involved in SVG Template to SVG Image Conversion

- **Fetch SVG Template**
    - Extracts the svg template url from the render method
        - Downloads the SVG XML string.
- **PreProcess Template**
  Preprocess SVG template for the Placeholders which needs some processing before replacing the placeholders.

    - **Replace QR Code**
        - Generates the QR code using Pixelpass library and replaces the `qrCodeImage` placeholder

    - **Transform Benefits Array to Multi line text**
        - Replace the benefits value placeholder with comma separated elements of Benefits array,
        -  We are splitting the whole comma separate benefits string into two lines through code to accommodate in the svg template design and replacing two placeholders {{benefits1}} and {{benefits2}}.
        - SVG Template must have the placeholders like {{benefits1}}, {{benefits2}} and so on as many as the number of lines they want to split the comma separated benefits string.
        - Example

      ```
      val vcJson = {"credentialSubject" : "benefits": ["Critical Surgery", "Full Health Checkup", "Testing"]}
      
      val svgTempalte = "<svg>{{benefits1}} {{benfits2}}</svg>"
      
      val result = transformArrayFieldsIntoMultiline(vcJson, svgTemplate, MultiLineProperties(10, listOf("{{benefits1}}", "{{benfits2}}")))
      //result => "<svg>Critical Surgery", "Full Health Checkup", "Testing</svg>"
  
      ```

     - **Transform Address Fields  into Multi line text**
        - Check for the address fields and create comma separated full Address String.
        - Replace the fullAddress value placeholder with separated elements of full Address String
        - We are splitting the whole comma separate full Address string into two lines through code to accommodate in the svg template design and replacing two placeholders with locales {{fullAddress1_eng}} and {{fullAddress1_eng}}.
        - SVG Template must have the placeholders like {{fullAddress1_eng}}, {{fullAddress1_eng}} and so on as many as the number of lines they want to split the comma separated address string.
    - Example

      ```
      val vcJson = {      "credentialSubject": {          "addressLine1": [{"value": "No 123, Test Address line1", "language": "eng"}],          "addressLine2": [{"value": "Test Address line", "language": "eng"}],          "city": [{"value": "TestCITY", "language": "eng"}],          "province": [{"value": "TESTProvince", "language": "eng"}],      }  }
      
      val svgTempalte = "<svg>{{fullAddress1_eng}} {{fullAddress2_eng}}</svg>"
      
      val result = transformAddressFieldsIntoMultiline(vcJson, svgTemplate, MultiLineProperties(10, listOf("{{fullAddress1_eng}}", "{{fullAddress2_eng}}")))
      //result => <svg>No 123, Test Address line1,Test Address line, TestCITY, TESTProvince  </svg>
      ```

    - **Replacing other placeholders with VC Data**
        - When the placeholder has locale like "{{credentialSubject/gender_eng}}", Replace the placeholders with appropriate locale value.
          ```
          val vcJson = {      "credentialSubject": {          "gender": [{"value": Male", "language": "eng"}]}
          
          val svgTempalte = "<svg>{{credentialSubject/gender_eng}}</svg>"
          
          //result => <svg>Male</svg>
          ```
        - Placeholders without locale will be directly replaced with Vc Data Json Path following the JSON Pointer Algorithm.
          ```
          val vcJson = {      "credentialSubject": {          "fullName": "Tester"}
          
          val svgTempalte = "<svg>{{credentialSubject/fullName}}</svg>"
          
          //result => <svg>Tester</svg>
          ```

    - **Returns the final replaced SVG Image**