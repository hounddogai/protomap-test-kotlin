package com.acme.transport.network.servicearea.areaassignment

fun serviceAreaAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a service area agreement code in its canonical SAA- form. */
fun normalizeServiceAreaAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAA-")) {
        return clean
    }
    return "SAA-${clean.replace(" ", "-")}"
}
