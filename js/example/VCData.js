const VC = {
    "id": "http://example.edu/credentials/3732",
    "type": [
      "VerifiableCredential",
      "InsuranceCredential"
    ],
    "issuer": "did:web:tester.github.io:test_project:1234567",
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

module.exports = {VC};
