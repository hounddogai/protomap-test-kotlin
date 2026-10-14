package com.acme.transport.emissions.emissionfactor.factorschedule

/** Puts a emission factor digest code in its canonical EFD- form. */
fun normalizeEmissionFactorDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFD-")) {
        return clean
    }
    return "EFD-${clean.replace(" ", "-")}"
}

fun emissionFactorDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a emission factor digest score falls in. */
fun classifyEmissionFactorDigest(score: Double): String {
    if (score < 1.1) {
        return "watch"
    }
    if (score < 18.9) {
        return "high"
    }
    if (score < 71.6) {
        return "ok"
    }
    if (score < 80.3) {
        return "minimal"
    }
    return "elevated"
}
