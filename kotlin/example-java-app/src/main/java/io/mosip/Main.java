package io.mosip;

import java.util.List;

import io.mosip.injivcrenderer.InjiVcRenderer;

public class Main {
    public static void main(String[] args) {

        InjiVcRenderer injiVcRenderer = new InjiVcRenderer("sample-java-app");
        List<String> svgImage = injiVcRenderer.renderVC(farmerVc);
        System.out.println(":::::Replaced Template-->"+svgImage);

    }

    static String farmerVc = """
            {
                      "renderMethod": [
                             {
                               "template": {
                                 "digestMultibase": "zB7zqWmE5vGRmAfD39XPWsFo6hvPyrk8QJTtaRqrbjM6t",
                                 "mediaType": "image/svg+xml",
                                 "id": "https://<host-url>/templates/farmer-with-face-rfc-compliance.svg"
                               },
                               "renderSuite": "svg-mustache",
                               "type": "TemplateRenderMethod"
                             }
                           ],
                      "credentialSubject": {
                          "ownershipType": "Tenant",
                          "idType": "Farmer ID",
                          "address": {
                              "district": "Lucknow",
                              "state": "Uttar Pradesh",
                              "village": "Gomti Nagar"
                          },
                          "gender": "Female",
                          "primaryCommodity": "Rice",
                          "fullName": "Mary Smith",
                          "dateOfBirth": "1985-11-12",
                          "crops": [
                              {
                                  "cropName": "Rice",
                                  "season": "rabi"
                              },
                              {
                                  "cropName": "Wheat",
                                  "season": "rabi"
                              }
                          ],
                          "farmerId": "8267411578",
                          "phoneNumber": "8765432109",
                          "authority": "Agro Veritas Authority",
                          "totalLandArea": {
                              "unit": "acres",
                              "value": 2.5
                          },
                          "id": "did:jwk:eyJrdHkiOiJSU0EiLCJlIjoiQVFBQiIsInVzZSI6InNpZyIsImtpZCI6ImR0RVd5ek1BdGxQRWpZU0pPUG5ZZkdYM3p1QUV5SEt5YVFNdW5aaWRvdnciLCJhbGciOiJSUzI1NiIsIm4iOiJnQ0Z0TWZFOXI2Y0YwaDUzYjVxc0hnQk5UZHJzNDByOFZmTXN1a2J4MUZUOUpMX1loRU9zeHJXQkp5OG1OOE95MXROUjQ0VnE5djNXY2pvOEJYUUpkNU9QVzBlZHhkcUtaUjdKODhPRGVXNmltUlJYTG1hQUdERGwtZWUtMVljZEgyNVZ0ajI4TkdiUlAxNS1URU0tcktGb09VMXlaY09fMUxmS0FIcXQ2dDZIaG12ZzZIZlRBOEctZUk1dzdaMThnS25WYzJxc2hxMkZ6R1VaVDNZS2tETkpTVDJOd1dUOVRkeUQ1RlVVd09aa1Yycjd5OWtSQ2EwLXpqc0JoX0VKRFhGS0k3VmNIR0NpNmdEUzkyVHdWdE5ZQklvaDV0THRBUURSM2RvRDFGbWdHejByZHZHY3BkejRPVUJJelpVWkRmMFA4SzdJUmoxcENlNm5XTlJVRFEifQ=="
                      },
                      "validUntil": "2027-08-29T11:31:41.659Z",
                      "validFrom": "2025-08-29T11:31:41.659Z",
                      "id": "https://mosip.io/credential/5a93441d-ef62-4bd1-baae-1b99e6e3db3c",
                      "type": [
                          "VerifiableCredential",
                          "FarmerCredential"
                      ],
                      "@context": [
                          "https://www.w3.org/ns/credentials/v2",
                          "https://mosip.github.io/inji-config/contexts/farmer-context.json",
                          "https://w3id.org/security/suites/ed25519-2020/v1"
                      ],
                      "issuer": "did:web:mosip.github.io:inji-config:dev-int-inji:farmer",
                      "credentialStatus": {
                          "statusPurpose": "revocation",
                          "statusListIndex": "14",
                          "id": "https://injicertify-farmer.dev-int-inji.mosip.net/v1/certify/credentials/status-list/db16a0ac-6f46-47eb-803b-8197ae27720d#14",
                          "type": "BitstringStatusListEntry",
                          "statusListCredential": "https://injicertify-farmer.dev-int-inji.mosip.net/v1/certify/credentials/status-list/db16a0ac-6f46-47eb-803b-8197ae27720d"
                      },
                      "proof": {
                          "type": "Ed25519Signature2020",
                          "created": "2025-08-29T11:31:41Z",
                          "proofPurpose": "assertionMethod",
                          "verificationMethod": "did:web:mosip.github.io:inji-config:dev-int-inji:farmer#rYCXjgs8nPGqrVv75z-pKACsUw9VlA2WLUPbCjuGn6Q",
                          "proofValue": "z5yUp9Dyx5Ts5GDjek8wWBEyJvrNuDH6GoCZsHMTD4fzaYUDyPN21t6fnyNAbDiGu3B5QZEcvbRYoddRpnUHXcWnn"
                      }
                  }
            """;
}
