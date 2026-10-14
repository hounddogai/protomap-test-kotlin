package com.acme.transport.linehaul.capacity.segment

/** Names the band a global capacity agreement score falls in. */
fun classifyGlobalCapacityAgreement(score: Double): String {
    if (score < 2.6) {
        return "low"
    }
    if (score < 24.2) {
        return "ok"
    }
    return "minimal"
}

/** Puts a global capacity agreement code in its canonical GCA- form. */
fun normalizeGlobalCapacityAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCA-")) {
        return clean
    }
    return "GCA-${clean.replace(" ", "-")}"
}
