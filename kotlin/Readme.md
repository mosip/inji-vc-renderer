## InjiVcRenderer - Kotlin Library
- A Kotlin library to convert SVG Template to SVG Image by replacing the placeholders in the SVG Template with actual Verifiable Credential Json Data. Strictly follows JSON Pointer Algorithm RFC6901 to extract the values from the VC.

### Features
- Downloads the SVG template from the renderMethod field of the VC to support VC Data Model 2.0.
- Replace the placeholders in the SVG template with actual VC Json Data.
- Generates aar and jar from the library .

### Build
- Modules in the Kotlin Project
1. example-android-app
    - Application that uses **injivcrenderer** library project to print Updated SVG Template.
    - Update the test data of VC with valid SVG Host URL.
    - Run using  `./gradlew :example-android-app:build`
2. example-java-app
    - Application that uses **injivcrenderer** library project to print Updated SVG Template.
    - Update the test data of VC with valid SVG Host URL.
    - Run using  `./gradlew :example-java-app:build`
3. injivcrenderer
       - Library to replace the placeholders in the Svg Template received from the renderMethod with the actual Verifiable Credential
       - Run using `./gradlew :injivcrenderer:assembleRelease` to generate the aar
       - Gradle task is registered to generate jar by running the command `./gradlew :injivcrenderer:build` which creates jar in the `build/libs` folder
       - Run Tests using `./gradlew testDebugUnitTest` or `./gradlew testReleaseUnitTest` based on the build type.

### API
- `renderVC(credentialFormat: CredentialFormat, wellKnownJson: String? = null, vcJsonString: String)` - expects the Verifiable Credential, Well-known Json and Credential Format as input and returns the list of replaced SVG Templates.
    - `vcJsonData` - VC Downloaded in stringified format.
    - `wellKnownJson` - Well-known Json downloaded in stringified format. It is optional parameter.
    - `credentialFormat` - Enum to specify the credential format. Currently only LDP_VC format is supported.
- This method takes entire VC data as input.
- Example :
```
        val vcJson = """{
            "credentialSubject": {
                "fullName": "John",
                "gender": [
                    "language": "eng",
                    "value": "Male"
                ] 
            },
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/sample.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        }"""
        // Assume SVG Template hosted is "<svg lang="eng">{{/credentialSubject/gender}}##{{/credentialSubject/fullName}}</svg>"
    Result will be => [<svg lang="eng">Male##John</svg>]
```
- Returns the Replaced svg template to render proper SVG Image. It list of SVG Template if multiple render methods are present in the VC.


## Package Structure
```
io.mosip.injivcrenderer/commonMain
├── InjiVcRenderer.kt                  # Main library class with public API
├── constants/         # Constants used across the library
│   ├── Constants.kt   
│   ├── NetworkConstants.kt      
│   └── VcRendererErrorCodes.kt #Error codes used for Custom Exceptions              
│   |
├── exceptions/        # Exceptions
│   ├── VcRendererExceptions.kt  # Centralized exception definitions
│   │
│── qrCode/          
│   │   ├── QRCodeGenerator.kt  # QR code generation utility
│   │   └── QrDataConvertor.kt # Implementation of QR code generation
│── templateEngine/svg/        # Json Pointer Algorithm implementation
    |--JsonPointerResolver.kt    
├── utils      # Utility classes
|    ├── Utils.kt               # SVG related utilities
├── networkManager     
    ├── NetworkManager.kt               # Network related utilities
```

###### Exceptions

1. InvalidRenderSuiteException is thrown if render suite is not `svg-mustache`
2. InvalidRenderMethodTypeException is thrown if render method type is not `TemplateRenderMethod`
3. QRCodeGenerationFailureException is thrown if QR code generation fails
4. MissingTemplateIdException is thrown if template id is missing in render method
5. SvgFetchException is thrown if fetching SVG from the URL fails
6. InvalidRenderMethodException is thrown if render method object is invalid


### Steps involved in SVG Template to SVG Image Conversion
- Render Method Extraction from VC
  - Extracts the render method from the VC Json data.
  - If multiple render methods are present, it will process all the render methods and return the list of replaced SVG Templates.


#### Downloading SVG Template from URL in VC
  - If Render Method object has `template` field as object with `id` field as url and `mediaType` as `image/svg+xml`, SVG Template needs to be downloaded from the URL and then replace the placeholders.
      ```
          "renderMethod": {
          "type": "TemplateRenderMethod",
          "renderSuite": "svg-mustache",
          "template": {
                  "id": "https://degree.example/credential-templates/bachelors",
                  "mediaType": "image/svg+xml",
                  "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
              }
          }
      ```
 - Render method type should be `TemplateRenderMethod` and render suite should be `svg-mustache`.
- Note : Embedded SVG Template and hosting render method as jsonld document are not supported in this library. Hosting the SVG Template as URL is supported.

#### Preprocessing the SVG Template

##### QR Code Placeholder
  - If the SVG Template has `{{/qrCodeImage}}` , it will generate the QR code using Pixelpass library and replace the placeholder with generated QR code image in base64 format.
    - Example:
        ```
        val vcJson = {"credentialSubject" : "id": "did:example:123456789", "name": "Tester"}
        
        val svgTempalte = "<svg><image id = "qrCodeImage" href="{{/qrCodeImage}}"</svg>"
        
        //result => <svg><image id = "qrCodeImage" href="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAAABmJLR0QA/wD/AP+gvaeTAAAIKklEQVR4nO3de5QdZZnv8e9M7MzMzM7szszM7s"
      
- Note: It is mandatory to have `id` field in the `<image>` as `qrCodeImage` and placeholder as `{{/qrCodeImage}}` to generate the QR code.
      

##### Handling Render Property
  - If the `template` field is an object and has `renderMethod` property. Property in the `renderMethod` will be taken into consideration for further processing and rest of the fields placeholders will be replaced with empty string.
    - Example:
        ```
          "renderMethod": {
              "type": "TemplateRenderMethod",
              "renderSuite": "svg-mustache",
              "template": {
                      "id": "https://example.edu/credential-templates/BachelorDegree",
                      "mediaType": "image/svg+xml",
                      "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR",
                      "renderProperty": [
                        "/issuer", "/validFrom", "/credentialSubject/degree/name"
                      ]
                  }
          }
        ```
    - In the above example, only the fields `issuer`, `validFrom` and `credentialSubject/degree/name` will be considered for replacing the placeholders in the SVG Template.
  - If `renderProperty` is not present, all the fields in the VC will be considered for replacing the placeholders in the SVG Template.

##### Array Fields Handling
- For array fields in the VC, index based approach will be followed.
- Example:
    ```
    val vcJson = {"credentialSubject" : "benefits": ["Critical Surgery", "Full Health Checkup", "Testing"]}
    
    val svgTempalte = "<svg>{{/benefits/0}},{{/benefits/1}}</svg>"
    
    //result => <svg>Critical Surgery,Full Health Checkup</svg>
    ```
- Example for array of objects:
    ```
    val vcJson = {      "credentialSubject": {          "awards": [              {"title": "Award1", "year": "2020"},              {"title": "Award2", "year": "2021"}          ]      }  }
    
    val svgTemplate = "<svg>{{/credentialSubject/awards/0/title}} - {{/credentialSubject/awards/0/year}}, {{/credentialSubject/awards/1/title}} - {{/credentialSubject/awards/1/year}}</svg>"
    
    //result => <svg>Award1 - 2020, Award2 - 2021</svg>
    ```

##### Locale Handling
- For locale handling, same JSON Pointer Algorithm is used to extract the value from the VC.
- Example:
    ```
    val vcJson = {      "credentialSubject": { "fullName": "Tester", "city": [{"value": "TestCITY", "language": "eng"},{"value": "VilleTest", "language": "fr"}]}
          
      val svgTempalte = "<svg>{{/credentialSubject/fullName}} - {{/credentialSubject/city/0/value}}</svg>"
          
      //result => <svg>Tester - TestCITY</svg>
  ```

##### Wellknown fallback handling
- If placeholder for label is present in the SVG Template and concern path is not available in well-known or well-known itself not available, it will check for `/credential_definition/credentialSubject` in th placeholder and takes the path next to that as the value to replace it.
- Example:
    ```
    //Well-known is not available
    val vcJson = {      "credentialSubject": { "fullName": "Tester", "city": [{"value": "TestCITY", "language": "eng"},{"value": "VilleTest", "language": "fr"}]}
          
      val svgTempalte = "<svg>{{/credential_definition/credentialSubject/fullName}} - {{/credentialSubject/fullName/0/value}}</svg>"
          
      //result => <svg>Full Name - Tester</svg>
  ```
Note: camelCase, PascalCase or snake_case value is converted to Title Case for the label. e.g. fullName or FullName or full_name is converted to Full Name.

##### Digest Multibase Validation
- If the `digestMultibase` field is present in the `template` object, it will validate the downloaded SVG Template using the digestMultibase value.
- `MultibaseVerificationException` is thrown if the validation fails or digestMultibase is invalid.
- Example:
    ```
          "renderMethod": {
              "type": "TemplateRenderMethod",
              "renderSuite": "svg-mustache",
              "template": {
                      "id": "https://degree.example/credential-templates/bachelors",
                      "mediaType": "image/svg+xml",
                      "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                  }
          }
      ```
- As per spec `digestMultibase` is optional field and it should follow below standard if present.
    - An OPTIONAL multibase-encoded Multihash of the render method referenced if id is specified. The multibase value MUST be u (base64url-nopad) and the multihash value MUST be SHA-2 with 256-bits of output (0x12).

#### Replacing Placeholders in SVG Template
- Replaces the placeholders in the SVG Template with actual VC Json Data strictly follows JSON Pointer Algorithm RFC6901.
- Returns the list of replaced SVG Templates if multiple render methods are present in the VC.


### References
- [JSON Pointer Algorithm - RFC6901](https://www.rfc-editor.org/rfc/rfc6901)
- [Draft Implementation of Verifiable Credential Rendering Methods](https://w3c-ccg.github.io/vc-render-method/#the-rendermethod-property)
- [Data model 2.0 implementation](https://www.w3.org/TR/vc-data-model-2.0/#reserved-extension-points)