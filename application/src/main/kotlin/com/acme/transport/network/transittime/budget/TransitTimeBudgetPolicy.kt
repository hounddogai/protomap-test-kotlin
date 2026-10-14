package com.acme.transport.network.transittime.budget

/** Puts a transit time budget code in its canonical TTB- form. */
fun normalizeTransitTimeBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTB-")) {
        return clean
    }
    return "TTB-${clean.replace(" ", "-")}"
}

/** Names the band a transit time budget score falls in. */
fun classifyTransitTimeBudget(score: Double): String {
    if (score < 5.7) {
        return "high"
    }
    if (score < 43.4) {
        return "ok"
    }
    if (score < 57.9) {
        return "minimal"
    }
    return "watch"
}
