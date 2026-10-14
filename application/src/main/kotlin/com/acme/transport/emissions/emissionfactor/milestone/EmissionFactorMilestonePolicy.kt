package com.acme.transport.emissions.emissionfactor.milestone

/** Names the band a emission factor milestone score falls in. */
fun classifyEmissionFactorMilestone(score: Double): String {
    if (score < 4.8) {
        return "critical"
    }
    if (score < 47.6) {
        return "low"
    }
    return "minimal"
}

/** Puts a emission factor milestone code in its canonical EFM- form. */
fun normalizeEmissionFactorMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFM-")) {
        return clean
    }
    return "EFM-${clean.replace(" ", "-")}"
}
