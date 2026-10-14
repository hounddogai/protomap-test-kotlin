package com.acme.transport.customs.clearance.estimate

/** Puts a weighted clearance estimate code in its canonical WCE- form. */
fun normalizeWeightedClearanceEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WCE-")) {
        return clean
    }
    return "WCE-${clean.replace(" ", "-")}"
}

/** Names the band a weighted clearance estimate score falls in. */
fun classifyWeightedClearanceEstimate(score: Double): String {
    if (score < 1.8) {
        return "watch"
    }
    if (score < 10.0) {
        return "elevated"
    }
    if (score < 27.5) {
        return "critical"
    }
    if (score < 76.5) {
        return "low"
    }
    return "minimal"
}
