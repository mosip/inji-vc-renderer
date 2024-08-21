### InjiVcRenderer

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
#### API
- `replaceSVGTemplatePlaceholders(vcJsonData: String)` - expects the Verifiable Credential as parameter and returns the replaced SVG Template.
    - `vcJsonData` - VC Downloaded in stringified format.