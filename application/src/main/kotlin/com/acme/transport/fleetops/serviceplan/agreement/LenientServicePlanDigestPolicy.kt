package com.acme.transport.fleetops.serviceplan.agreement

/** Names the band a lenient service plan digest score falls in. */
fun classifyLenientServicePlanDigest(score: Double): String {
    if (score < 28.1) {
        return "medium"
    }
    if (score < 47.9) {
        return "high"
    }
    if (score < 96.0) {
        return "elevated"
    }
    return "watch"
}

/** Puts a lenient service plan digest code in its canonical LSPD- form. */
fun normalizeLenientServicePlanDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSPD-")) {
        return clean
    }
    return "LSPD-${clean.replace(" ", "-")}"
}
