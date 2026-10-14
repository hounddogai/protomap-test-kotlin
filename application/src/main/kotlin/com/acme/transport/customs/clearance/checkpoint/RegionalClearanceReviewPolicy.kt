package com.acme.transport.customs.clearance.checkpoint

/** Names the band a regional clearance review score falls in. */
fun classifyRegionalClearanceReview(score: Double): String {
    if (score < 9.1) {
        return "high"
    }
    if (score < 34.9) {
        return "critical"
    }
    if (score < 38.7) {
        return "elevated"
    }
    return "medium"
}

/** Puts a regional clearance review code in its canonical RCR- form. */
fun normalizeRegionalClearanceReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCR-")) {
        return clean
    }
    return "RCR-${clean.replace(" ", "-")}"
}
