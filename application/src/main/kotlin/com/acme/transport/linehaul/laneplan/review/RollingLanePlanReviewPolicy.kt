package com.acme.transport.linehaul.laneplan.review

/** Names the band a rolling lane plan review score falls in. */
fun classifyRollingLanePlanReview(score: Double): String {
    if (score < 18.9) {
        return "minimal"
    }
    if (score < 34.9) {
        return "medium"
    }
    if (score < 78.4) {
        return "ok"
    }
    if (score < 90.0) {
        return "critical"
    }
    return "elevated"
}

/** Puts a rolling lane plan review code in its canonical RLPR- form. */
fun normalizeRollingLanePlanReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RLPR-")) {
        return clean
    }
    return "RLPR-${clean.replace(" ", "-")}"
}
