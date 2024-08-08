const VC = {
    "id": "http://example.edu/credentials/3732",
    "type": [
      "VerifiableCredential",
      "InsuranceCredential"
    ],
    "issuer": "did:web:holashchand.github.io:test_project:32b08ca7-9979-4f42-aacc-1d73f3ac5322",
    "@context": [
      "https://www.w3.org/ns/credentials/v2",
      "https://www.w3.org/ns/credentials/examples/v2",
      "https://w3id.org/vc/render-method/v1"
    ],
    "issuanceDate": "2024-05-09T09:10:05.450Z",
    "expirationDate": "2024-06-08T09:10:05.426Z",
    "credentialSubject": {
      "id": "did:jwk:example",
      "dob": "2000-01-26",
      "email": "tgs@gmail.com",
      "gender": "Male",
      "mobile": "0123456789",
      "benefits": [
        "Critical PSUT",
        "Hepatitas PSUT"
      ],
      "fullName": "TGSStudio",
      "policyName": "Sunbird Insurenc Policy",
      "policyNumber": "55555",
      "policyIssuedOn": "2023-04-20",
      "policyExpiresOn": "2033-04-20"
    },
    "renderMethod": [{
      "id": "https://<svg-template-host-url>_svg_template.svg",
      "type": "SvgRenderingTemplate",
      "name": "Portrait Mode",
      "css3MediaQuery": "@media (orientation: portrait)",
      "digestMultibase": "zQmAPdhyxzznFCwYxAp2dRerWC85Wg6wFl9G270iEu5h6JqW"
    }]
  }

const svgTemplate = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"250\" height=\"400\" viewBox=\"0 0 250 400\">\n" +
    "<text x=\"20\" y=\"60\" fill = \"#0000ff\" font-size=\"18\" font-weight=\"bold\">Hi {{credentialSubject/name}}</text>\n" +
    "</svg>";

module.exports = {VC, svgTemplate};
