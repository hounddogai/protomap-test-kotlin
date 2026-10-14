package com.acme.transport.customs.hold.digest

/** Puts a manual hold estimate code in its canonical MHE- form. */
fun normalizeManualHoldEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MHE-")) {
        return clean
    }
    return "MHE-${clean.replace(" ", "-")}"
}

/** Names the band a manual hold estimate score falls in. */
fun classifyManualHoldEstimate(score: Double): String {
    if (score < 3.4) {
        return "critical"
    }
    if (score < 5.7) {
        return "minimal"
    }
    if (score < 61.5) {
        return "low"
    }
    if (score < 62.0) {
        return "elevated"
    }
    return "ok"
}
