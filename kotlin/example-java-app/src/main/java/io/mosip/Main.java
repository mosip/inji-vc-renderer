package io.mosip;

import io.mosip.injivcrenderer.InjiVcRenderer;

public class Main {
    public static void main(String[] args) {

        InjiVcRenderer injiVcRenderer = new InjiVcRenderer();
        String svgImage = injiVcRenderer.renderSvg(insuranceVc);
        System.out.println(":::::Replaced Template-->"+svgImage);

    }

    static String insuranceVc = """
                {
                    "credentialSubject": {
                        "id": "",
                        "dob": "2000-01-26",
                        "email": "tgs@gmail.com",
                        "gender": "Male",
                        "mobile": "0123456789",
                        "fullName": "TGSStudio",
                        "policyName": "Sunbird Insurenc Policy",
                        "policyNumber": "55555",
                        "policyIssuedOn": "2023-04-20",
                        "policyExpiresOn": "2033-04-20",
                        "benefits": [
                            "Critical Surgery",
                            "Full body checkup"
                        ]
                    },
                    "renderMethod" : [
                            {
                              "id": "https://<svg-host-url>/assets/templates/national_id_template_without_qr.svg",
                              "type": "SvgRenderingTemplate",
                              "name": "Portrait Mode"
                            }
                          ]
                }
            """;
}