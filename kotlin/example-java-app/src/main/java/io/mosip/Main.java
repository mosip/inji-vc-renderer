package io.mosip;

import java.util.List;

import io.mosip.injivcrenderer.InjiVcRenderer;

public class Main {
    public static void main(String[] args) {

        InjiVcRenderer injiVcRenderer = new InjiVcRenderer();
        List<String> svgImage = injiVcRenderer.renderSvg(insuranceVc);
        System.out.println(":::::Replaced Template-->"+svgImage);

    }

    static String insuranceVc = """
            {
                     "id": "did:rcw:834be61c-67f7-48ba-9945-adefb7ec0f1f",
                     "type": [
                         "VerifiableCredential",
                         "InsuranceCredential"
                     ],
                     "proof": {
                         "type": "Ed25519Signature2020",
                         "created": "2025-08-31T19:57:04Z",
                         "proofValue": "z4A2RJ8d9CFZNhkHtkR2rovMVwx9gmBDGwh6QSmhW56ZUfmEGinrg1PmNn4beUrdRoFny7G1mfnDXg7smPiXcyz5d",
                         "proofPurpose": "assertionMethod",
                         "verificationMethod": "did:web:api.released.mosip.net:identity-service:02b073b8-aacd-472e-b63f-265bb7ccdd9f#key-0"
                     },
                     "issuer": "did:web:api.released.mosip.net:identity-service:02b073b8-aacd-472e-b63f-265bb7ccdd9f",
                     "@context": [
                         "https://www.w3.org/2018/credentials/v1",
                         "https://holashchand.github.io/test_project/insurance-context.json",
                         "https://w3id.org/security/suites/ed25519-2020/v1"
                     ],
                     "issuanceDate": "2025-08-31T19:57:04.275Z",
                     "expirationDate": "2025-09-30T19:57:04.270Z",
                     "credentialSubject": {
                         "id": "did:jwk:eyJrdHkiOiJSU0EiLCJlIjoiQVFBQiIsInVzZSI6InNpZyIsImtpZCI6InVVWTQxeGI1X3JMbVVmaDhUQ3hxQnBEQVI5UVJXVURKMkFURW44TGs4cEEiLCJuIjoidVk1eUNFbEpPTUNTSFNHdjlUVkNDMUVoSEF0SV91TmpIS3czT3h3SktnZTVDVzJMa0JSRkhHcWpYbTB6amdWVTZPSUFOdllQNjZ6dGVvaV9KWkRvNmhzcjBoT2JCZnRhd01Md01vemlsOHpITjVwQ2tpYjhaY01Udkg0NFBXRjRVNTRDZDU4dVp4VFZ1OUN6dXFEREk4UVF0Z2ktcnF1WjhQV3BRYVVWbjNfY0VoOEJJN2NpcFhZRF9BVkZnQ3VHZzhZT2VaOVY2NGEzRkg2bWZCWTNhemp0VWttOTNUeF96ZXNQSVNMMUkyY2ZGQnR4TVlQWWxiQ05VM0ZfeGlFZ0cxSWc0MHZPcEhzeXpFMVJqNC1JSlBscHBfekN2RktySzg5ZlRfY3p1aFJweHNFMHo0c1RjeElwOEpYVEMzb2xnREt2aTNkVU9YUXJHOUE3bm1NN193In0=",
                         "dob": "2025-01-01",
                         "email": "abcd@gmail.com",
                         "gender": "Male",
                         "mobile": "0123456789",
                         "benefits": [
                             "Critical Surgery",
                             "Full body checkup"
                         ],
                         "fullName": "wallet",
                         "policyName": "wallet",
                         "policyNumber": "5555",
                         "policyIssuedOn": "2023-04-20",
                         "policyExpiresOn": "2033-04-20"
                     },
                     "renderMethod": [
                         {
                             "template": {
                                 "digestMultibase": "zB7zqWmE5vGRmAfD39XPWsFo6hvPyrk8QJTtaRqrbjM6t",
                                 "mediaType": "image/svg+xml",
                                 "id": "https://75b2315daedd.ngrok-free.app/templates/farmer-svg-template-with-qr-code.svg"
                             },
                             "renderSuite": "svg-mustache",
                             "type": "TemplateRenderMethod"
                         }
                     ]
                 }
             }
            """;
}
