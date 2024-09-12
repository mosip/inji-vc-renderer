const InsuranceVC = {
    "@context": [
        "https://test/credentials/v1",
        "https://test.github.io/test_project/test-context.json",
        {
            "LifeInsuranceCredential": {
                "@id": "InsuranceCredential"
            }
        },
        "https://w3id.org/test/suites/test-2020/v1"
    ],
    "credentialSubject": {
        "id": "did:jwk:eyJrdHJ9",
        "dob": "1986-06-17",
        "email": "swati@gmail.com",
        "gender": "Female",
        "mobile": "0123456789",
        "benefits": [
            "Critical Surgery",
            "Full body checkup",
            "Critical Surgery",
            "Full body checkup",
            "Critical Surgery",
            "Full body checkup",
            "Critical Surgery",
            "Full body checkup"
        ],
        "fullName": "Swati",
        "policyName": "Start Insurance Gold Premium",
        "policyNumber": "8793000",
        "policyIssuedOn": "2024-07-16",
        "policyExpiresOn": "2034-07-16"
    },
    "expirationDate": "2024-09-29T05:20:45.314Z",
    "id": "did:rcw:f15ba634-2e96-4a6a-ade1",
    "issuanceDate": "2024-08-30T05:20:45.331Z",
    "issuer": "did:web:test.net:identity-service:d4bf",
    "proof": {
        "created": "2024-08-30T05:20:46Z",
        "proofPurpose": "assertionMethod",
        "proofValue": "z5fu4NnceRbF1X45xgcaS",
        "type": "Ed25519Signature2020",
        "verificationMethod": "did:web:test.net:identity-service:d4bf"
    },
    "type": [
        "VerifiableCredential",
        "LifeInsuranceCredential"
    ],
    "renderMethod": [
        {
            "id": "https://<svg-host-url>/assets/templates/insurance_template.svg",
            "type": "SvgRenderingTemplate",
            "name": "Portrait Mode"
        }
    ]
}

const MosipVCWithQR = {
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
            "id": "https://<svg-host-url>/assets/templates/national_id_template_with_qr.svg",
            "type": "SvgRenderingTemplate",
            "name": "Portrait Mode"
        }
    ]
}

const MosipVCWithoutQR = {
    "@context": [
        "https://credentials/v1",
        "https:///.well-known/ida.json",
        {
            "sec": "https://security#"
        }
    ],
    "credentialSubject": {
        "VID": "6532781704389407",
        "face": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAkubSH5karW5/9k=",
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
                "value": "TEST_CITYeng werwrewr wrwer werewr "
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
            "id": "https://<svg-host-url>/assets/templates/national_id_template_without_qr.svg",
            "type": "SvgRenderingTemplate",
            "name": "Portrait Mode"
        }
    ]
}

module.exports = {InsuranceVC, MosipVCWithQR, MosipVCWithoutQR};
