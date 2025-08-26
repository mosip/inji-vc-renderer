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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.mosip.injivcrenderer.ui.theme.InjiVcRendererJarTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

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

    var svgString by remember { mutableStateOf<String?>(null) }


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
                  "id": "https://<svg-host-url>/assets/templates/insurance_template.svg",
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
                },
                {
                    "language": "tam",
                    "value": "Tamil TEST_FULLNAMEeng"
                }
            ],
                        "gender": [
                                        {
                                            "language": "tam",
                                            "value": "TAM MLE"
                                        },
                            {
                                "language": "eng",
                                "value": "MLE"
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
                "id": "https://<svg-host-url>/assets/templates/national_id_template_without_qr.svg",
                "type": "SvgRenderingTemplate",
                "name": "Portrait Mode"
            }
        ]
    }
    """.trimIndent()

    val farmerVcJson = """
          {
              "@context": [
                "https://www.w3.org/2018/credentials/v1",
                "https://jainhitesh9998.github.io/tempfiles/farmer-credential.json",
                "https://w3id.org/security/suites/ed25519-2020/v1"
              ],
              "credentialSubject": {
                "id": "did:jwk:eyJrdHkiO",
                "fullName": "Ramesh",
                "farmerID": "3823333312345",
                "gender": "Male",
                "mobile": "9840298402",
                "email": "ramesh@mosip.io",
                "dob": "1980-01-24",
                "benefits": [
                  "Wheat",
                  "Corn"
                ],
                "primaryCommodity": "Paddy (Rice)",
                "ownershipType": "Owner",
                "crop": "Rice",
                "totalLandArea": "2.5 hectares"
              },
              "type": [
                "VerifiableCredential",
                "FarmerCredential"
              ],
              "renderMethod": [{
                "type": "TemplateRenderMethod",
                "renderSuite": "svg-mustache",
                "template": {
                  "id": "https://281230d9ab5e.ngrok-free.app/templates/farmer_front_final.svg",
                  "mediaType": "image/svg+xml",
                  "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                }
              },
              {
                "type": "TemplateRenderMethod",
                "renderSuite": "svg-mustache",
                "template": {
                  "id": "https://281230d9ab5e.ngrok-free.app/templates/farmer_rear_final.svg",
                  "mediaType": "image/svg+xml",
                  "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                }
              }
              ]
            }
    """.trimIndent()

    val scope = rememberCoroutineScope() // For launching background tasks safely


    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Hello World!", modifier = modifier)

        Button(onClick = {
            scope.launch {
                try {
                    // Perform network and heavy work on IO dispatcher
                    val replacedTemplate = withContext(Dispatchers.IO) {
                        InjiVcRenderer().renderSvg(farmerVcJson)
                    }
                    println("Replaced Template: $replacedTemplate")

                    // Update state on main thread
                    if (replacedTemplate.isNotEmpty()) {
                        svgString = replacedTemplate[0]
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }) {
            Text(text = "Famrer Vc")
        }

        Spacer(modifier = Modifier.height(16.dp))

        svgString?.let { svg ->
            val svgBytes = svg.toByteArray(Charsets.UTF_8) // Ensure UTF-8
            val context = LocalContext.current

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(ByteArrayInputStream(svgBytes)) // InputStream works
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
                contentDescription = "Rendered SVG",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
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