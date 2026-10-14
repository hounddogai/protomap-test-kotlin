package com.acme.transport.safety.scorecard.milestone

/** Names the band a manual scorecard bundle score falls in. */
fun classifyManualScorecardBundle(score: Double): String {
    if (score < 51.8) {
        return "elevated"
    }
    if (score < 57.3) {
        return "high"
    }
    if (score < 57.8) {
        return "watch"
    }
    return "medium"
}

fun manualScorecardBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
