package io.mosip.injivcrenderer.constants

enum class CredentialFormat(val value: String) {
    LDP_VC("ldp_vc");

    companion object {
        fun fromValue(value: String): CredentialFormat? {
            return entries.find { it.value == value }
        }
    }
}