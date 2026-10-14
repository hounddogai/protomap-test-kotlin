package com.acme.transport.emissions.emissionfactor.plan

/** Names the band a primary emission factor group score falls in. */
fun classifyPrimaryEmissionFactorGroup(score: Double): String {
    if (score < 16.5) {
        return "critical"
    }
    if (score < 68.2) {
        return "elevated"
    }
    if (score < 84.5) {
        return "medium"
    }
    if (score < 98.8) {
        return "high"
    }
    return "minimal"
}

/** Puts a primary emission factor group code in its canonical PEFG- form. */
fun normalizePrimaryEmissionFactorGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PEFG-")) {
        return clean
    }
    return "PEFG-${clean.replace(" ", "-")}"
}

fun primaryEmissionFactorGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
