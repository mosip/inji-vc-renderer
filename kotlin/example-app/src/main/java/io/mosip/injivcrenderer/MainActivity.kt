package io.mosip.injivcrenderer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.mosip.injivcrenderer.ui.theme.InjiVcRendererJarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InjiVcRendererJarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    val insuranceVcJson = """
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
                  "id": "https://<local-host>/insurance_svg_template.svg",
                  "type": "SvgRenderingTemplate",
                  "name": "Portrait Mode"
                }
              ]
    }
    """.trimIndent()

    val nationalIDVcJson = """
    {
        "@context": [
            "https://credentials/v1",
            "https:///.well-known/ida.json",
            {
                "sec": "https://security#"
            }
        ],
        "credentialSubject": {
            "VID": "6532781704389407",
            "face": "data:image/jpeg;base64,/9j/4",
            "gender": [
                {
                    "language": "eng",
                    "value": "MLE"
                }
            ],
            "phone": "+++7765837077",
            "city": [
                {
                    "language": "eng",
                    "value": "TEST_CITYeng"
                }
            ],
            "fullName": [
                {
                    "language": "eng",
                    "value": "TEST_FULLNAMEeng"
                }
            ],
            "addressLine1": [
                {
                    "language": "eng",
                    "value": "TEST_ADDRESSLINE1eng"
                }
            ],
            "dateOfBirth": "1992/04/15",
            "id": "did:jwk:eyJrdHkiOiJSU0EiL",
            "email": "mosipuser123@mailinator.com"
        },
        "id": "https://test.net/credentials/abcdefgh-a",
        "issuanceDate": "2024-09-02T17:36:13.644Z",
        "issuer": "https://test.netf/.well-known/controller.json",
        "proof": {
            "created": "2024-09-02T17:36:13Z",
            "jws": "eyJiNj",
            "proofPurpose": "assertionMethod",
            "type": "RsaSignature2018",
            "verificationMethod": "https://test/.well-known/public-key.json"
        },
        "type": [
            "VerifiableCredential",
            "TestVerifiableCredential"
        ],
        "renderMethod": [
            {
                "id": "https://<local-host>/insurance_svg_template.svg",
                "type": "SvgRenderingTemplate",
                "name": "Portrait Mode"
            }
        ]
    }
    """.trimIndent()
    Column() {
        Text(
            text = "Hello World!",
            modifier = modifier
        )
        Button(onClick = {
            val thread = Thread {
                try {
                    val replacedTemplate = InjiVcRenderer().renderSvg(nationalIDVcJson)
                    System.out.println("Replaced Template-->$replacedTemplate")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()
        }) {
            Text(text = "Replace")

        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InjiVcRendererJarTheme {
        Greeting("Android")
    }
}