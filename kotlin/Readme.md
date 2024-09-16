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
- **Fetch SVG Template**
    - Extracts the svg template url from the render method
    - Downloads the SVG XML string.

- **Replace QR Code**
    - Generates the QR code using Pixelpass library and replaces the `qrCodeImage` placeholder

- **Replace Benefits**
    - Replace the benefits value placeholder with comma separated elements of Benefits array,
    - Example:
  ```
  val vc = """
      {
      "credentialSubject": {
          "benefits": ["Medical Benefit", "Full Checkup", "Critical Inujury"]
      }
  }
  """
  val svgTemplate = "<svg>Policy Benefits : {{benefits1}}{{benefits2}}</svg>"

  const val = replaceBenefits(vc, svgTemplate)
  //result => <svg>Policy Benefits : Medical Benefit,Full Checkup,Critical Inujury</svg>
  
  ```
    - We are splitting the whole comma separate benefits string into two lines through code to accommodate in the svg template design and replacing two placeholders {{benefits1}} and {{benefits2}}.
- **Replace Address**
    - Check for the address fields and create comma separated full Address String.
    - Replace the fullAddress value placeholder with separated elements of full Address String
    - Example:
  ```
  val vc = """
      {
      "credentialSubject": {
          "addressLine1": [{"value": "No 123, Test Address line1"}],
          "addressLine2": [{"value": "Test Address line"}],
          "city": [{"value": "TestCITY"}],
          "province": [{"value": "TESTProvince"}],
      }
  }
  """
  val svgTemplate = "<svg>Full Address : {{fullAddress1}}{{fullAddress2}}</svg>"

  val result = replaceAddress(vc, svgTemplate)
  //result => "<svg>Full Address : No 123, Test Address line1,Test Address line,TestCITY,TESTProvince</svg>"
  
  ```
    - We are splitting the whole comma separate full Address string into two lines through code to accommodate in the svg template design and replacing two placeholders {{fullAddress1}} and {{fullAddress1}}.
- **Replacing other placeholders with VC Data**
    - Example
  ```
          val vc = """
      {
      "credentialSubject": {
          "email": "test@gmail.com",
      }
  }
  """
  val svgTemplate = "<svg>Email : {{credentialSubject/email}}</svg>"
  ```
- Returns the Replaced svg template to render proper SVG Image.