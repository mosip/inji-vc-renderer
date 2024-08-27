# JS Wrapper for INJI VC Renderer : 


### Running the Example App
- Go To js folder 
  - > cd js
- Run below command to run the example application
  - > npm run example
- To Modify the Template
  -  update "id" field with url pointing to svg template over here [sample-vc.js](example/sample-vc.js)
- To Modify the VC Data
  - update VC in [sample-vc.js](example/sample-vc.js)
- Run below command to run the tests
- > npm run test

### VC rendering Methods

- **Vcrenderer.renderSVG(vcData)** 
  - This method takes entire VC data as input.
  - Extracts the svg template url from the render method
  - Downloads the SVG XML string.
  - Parses the template with Credential Values
  - Generates the QR code and replaces the `qrCodeImage` placeholder
  - Returns the Parsed svg template
