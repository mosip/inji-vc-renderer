# JS Wrapper for INJI VC Renderer :

### Running the Example App

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

  

### VC rendering Method

  

- **Vcrenderer.renderSVG(vcData)**

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
		const vc = """
			{
			"credentialSubject": {
				"benefits": ["Medical Benefit", "Full Checkup", "Critical Inujury"]
			}
		}
		"""
		const svgTemplate = "<svg>Policy Benefits : {{benefits1}}{{benefits2}}</svg>"

		const result = replaceBenefits(vc, svgTemplate)
		//result => <svg>Policy Benefits : Medical Benefit,Full Checkup,Critical Inujury</svg>
		
		```
		- We are splitting the whole comma separate benefits string into two lines through code to accommodate in the svg template design and replacing two placeholders {{benefits1}} and {{benefits2}}.
	- **Replace Address**
		- Check for the address fields and create comma separated full Address String.
		- Replace the fullAddress value placeholder with separated elements of full Address String
		- Example: 
		```
		const vc = """
			{
			"credentialSubject": {
				"addressLine1": [{"value": "No 123, Test Address line1"}],
				"addressLine2": [{"value": "Test Address line"}],
				"city": [{"value": "TestCITY"}],
				"province": [{"value": "TESTProvince"}],
			}
		}
		"""
		const svgTemplate = "<svg>Full Address : {{fullAddress1}}{{fullAddress2}}</svg>"

		const result = replaceAddress(vc, svgTemplate)
		//result => "<svg>Full Address : No 123, Test Address line1,Test Address line,TestCITY,TESTProvince</svg>"
		
		```
		- We are splitting the whole comma separate full Address string into two lines through code to accommodate in the svg template design and replacing two placeholders {{fullAddress1}} and {{fullAddress1}}.
	- **Replacing other placeholders with VC Data**
		- Example
		```
				const vc = """
			{
			"credentialSubject": {
				"email": "test@gmail.com",
			}
		}
		"""
		const svgTemplate = "<svg>Email : {{credentialSubject/email}}</svg>"
		```
	- Returns the Replaced svg template to render proper SVG Image.