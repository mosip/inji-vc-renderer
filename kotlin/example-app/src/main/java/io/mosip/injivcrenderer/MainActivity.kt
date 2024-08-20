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

    val sampleJson = """
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
    Column() {
        Text(
            text = "Hello World!",
            modifier = modifier
        )
        Button(onClick = {
            val thread = Thread {
                try {
                    val replacedTemplate = InjiVcRenderer().renderSvg(sampleJson)
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