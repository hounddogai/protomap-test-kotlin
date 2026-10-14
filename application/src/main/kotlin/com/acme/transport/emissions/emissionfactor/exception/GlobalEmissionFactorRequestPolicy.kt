package com.acme.transport.emissions.emissionfactor.exception

/** Names the band a global emission factor request score falls in. */
fun classifyGlobalEmissionFactorRequest(score: Double): String {
    if (score < 7.2) {
        return "low"
    }
    if (score < 21.6) {
        return "ok"
    }
    if (score < 26.4) {
        return "medium"
    }
    if (score < 87.9) {
        return "elevated"
    }
    return "high"
}

/** Puts a global emission factor request code in its canonical GEFR- form. */
fun normalizeGlobalEmissionFactorRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GEFR-")) {
        return clean
    }
    return "GEFR-${clean.replace(" ", "-")}"
}
