package com.acme.transport.lastmile.proofofdelivery.recorditem

/** Names the band a global proof of delivery record score falls in. */
fun classifyGlobalProofOfDeliveryRecord(score: Double): String {
    if (score < 4.6) {
        return "low"
    }
    if (score < 37.1) {
        return "ok"
    }
    if (score < 67.9) {
        return "minimal"
    }
    if (score < 90.0) {
        return "elevated"
    }
    return "critical"
}

/** Puts a global proof of delivery record code in its canonical GPODR- form. */
fun normalizeGlobalProofOfDeliveryRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GPODR-")) {
        return clean
    }
    return "GPODR-${clean.replace(" ", "-")}"
}
