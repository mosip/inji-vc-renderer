## InjiVcRenderer - Kotlin Library
- A Kotlin library to convert SVG Template to SVG Image by replacing the placeholders in the SVG Template with actual Verifiable Credential Json Data.

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
       - Run using `./gradlew :injivcrenderer:build` to generate the aar
       - Gradle task is registered to generate jar by running the command `./gradlew jarRelease` which creates jar in the `build/libs` folder
       - Run Tests using `./gradlew testDebugUnitTest` or `./gradlew testReleaseUnitTest` based on the build type.

### API
- `renderSvg(vcJsonData: String)` - expects the Verifiable Credential as parameter and returns the replaced SVG Template.
    - `vcJsonData` - VC Downloaded in stringified format.
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


### Steps involved in SVG Template to SVG Image Conversion
- Render Method Extraction from VC
  - Extracts the render method from the VC Json data.
  - If multiple render methods are present, it will process all the render methods and return the list of replaced SVG Templates.

#### Embedding SVG Template in VC
- If Render Method object has `template` field as string, SVG Template is directly embedded in the VC.
  - Replaces the placeholders in the SVG Template with actual VC Json Data.
        ```
            "renderMethod": {
            "type": "TemplateRenderMethod",
            "renderSuite": "svg-mustache",
            "template": "data:image/svg+xml;base64,Qjei89...3jZpW"
            }
        ```
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
 - For both cases, render method type should be `TemplateRenderMethod` and render suite should be `svg-mustache`.

#### Preprocessing the SVG Template

##### QR Code Placeholder
  - If the SVG Template has `{{/qrCodeImage}}` placeholder, it will generate the QR code using Pixelpass library and replace the placeholder with generated QR code image in base64 format.
    - Example:
        ```
        val vcJson = {"credentialSubject" : "id": "did:example:123456789", "name": "Tester"}
        
        val svgTempalte = "<svg>{{/qrCodeImage}}</svg>"
        
        //result => <svg><image href="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAAABmJLR0QA/wD/AP+gvaeTAAAIKklEQVR4nO3de5QdZZnv8e9M7MzMzM7szszM7s"
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

##### Locale Handling
- If the template has locale like `<svg lang="eng">{{/credentialSubject/city}}</svg>`, extract the language from template, check for the locale object that has the language and then replace the placeholders with appropriate locale value.
- Example:
    ```
    val vcJson = {      "credentialSubject": { "fullName": "Tester", "city": [{"value": "TestCITY", "language": "eng"},{"value": "VilleTest", "language": "fr"}]}
          
      val svgTempalte = "<svg>{{/credentialSubject/fullName}} - {{/credentialSubject/city}}</svg>"
          
      //result => <svg>Tester - TestCITY</svg>
  ```
  
- If no language is provided in the SVG Template, it will look for the value from `eng` locale object and use it as default language .
- Example:
    ```
    val template = <svg>{{/credentialSubject/city}}</svg>
    val vcJson = {      "credentialSubject": { "city": [{"value": "Hindi City", "language": "hin"}, {"value": "English City", "language": "eng"},{"value": "French City", "language": "fr"}]}
          
      val svgTempalte = "<svg>{{/credentialSubject/city}}</svg>"
          
      //result => <svg>English City</svg>
  ```
-  If `eng` also not available , it will pick the first item from the language array.
- Example:
    ```
    val template = <svg>{{/credentialSubject/city}}</svg>
    val vcJson = {      "credentialSubject": { "city": [{"value": "Hindi City", "language": "hin"},{"value": "French City", "language": "fr"}]}
          
      val svgTempalte = "<svg>{{/credentialSubject/city}}</svg>"
          
      //result => <svg>Hindi City</svg>
  ```

##### Array Fields Handling
- For array fields in the VC, below approach will be followed.
- **chunkArrayFields**: Joins elements of a JSONArray into a single string, then wraps it into multiple lines for SVG. 
- **wrapText**: Core logic that splits text into lines without breaking words, based on the calculated character limit per line based on svg width, and wraps each line in a <tspan>.
- Example:
    ```
    val vcJson = {"credentialSubject" : "benefits": ["Critical Surgery", "Full Health Checkup", "Testing"]}
    
    val svgTempalte = "<svg>{{/benefits}}</svg>"
    
    //result => <svg><text><tspan>Critical Surgery, Full</tspan><tspan>Health Checkup, Testing</tspan></text></svg>
    ```

##### Concatenated Address Field Handling
- For svg Template with `{{/concatenatedAddress}}, below approach will be followed.
- **chunkAddressFields**: Wraps a single address string into multiple lines for SVG.
- **wrapText**: Core logic that splits text into lines without breaking words, based on the calculated character limit per line based on svg width, and wraps each line in a <tspan>.
- Example:
    ```
    val vcJson = {      "credentialSubject": {          "addressLine1": [{"value": "No 123, Test Address line1", "language": "eng"}],          "addressLine2": [{"value": "Test Address line", "language": "eng"}],          "city": [{"value": "TestCITY", "language": "eng"}],          "province": [{"value": "TESTProvince", "language": "eng"}],      }  }
    
    val svgTemplate = "<svg>{{/concatenatedAddress}}</svg>"
    
    //result => <svg><text><tspan>No 123, Test Address line1,</tspan><tspan>Test Address line, TestCITY, TESTProvince</tspan></text></svg>
    ```
- Here are the list of fields considered for concatenating address - `addressLine1`, `addressLine2`, `addressLine3`, `city`, `province`, `region` and `postalCode`

Note : For Array Fields and Concatenated Address , better to have the fields at the end of template or give enough height for the fields in the design. Because it will be wrapped based on the VC data for those fields. Because in SVG Template , there is limitation to do wrapping based on the content, so we are manually wrapping based the SVG Width



#### Replacing Placeholders in SVG Template
- Replaces the placeholders in the SVG Template with actual VC Json Data using JSON Pointer Algorithm.
- Returns the list of replaced SVG Templates if multiple render methods are present in the VC.


### References
- [Draft Implementation of Verifiable Credential Rendering Methods](https://w3c-ccg.github.io/vc-render-method/#the-rendermethod-property)
- [Data model 2.0 implementation](https://www.w3.org/TR/vc-data-model-2.0/#reserved-extension-points)