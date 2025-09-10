package io.mosip.injivcrenderer.constants

enum class CredentialFormat(val value: String) {
    LDP_VC("ldp_vc"),
    UNKNOWN("unknown");

    companion object {
        fun fromValue(value: String): CredentialFormat =
            entries.find { it.value == value } ?: UNKNOWN
    }
}