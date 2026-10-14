package com.acme.transport.safety.scorecard.allocation

/** Names the band a global scorecard allocation score falls in. */
fun classifyGlobalScorecardAllocation(score: Double): String {
    if (score < 8.1) {
        return "critical"
    }
    if (score < 37.1) {
        return "medium"
    }
    if (score < 53.4) {
        return "watch"
    }
    if (score < 64.8) {
        return "ok"
    }
    return "high"
}

/** Puts a global scorecard allocation code in its canonical GSA- form. */
fun normalizeGlobalScorecardAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GSA-")) {
        return clean
    }
    return "GSA-${clean.replace(" ", "-")}"
}
