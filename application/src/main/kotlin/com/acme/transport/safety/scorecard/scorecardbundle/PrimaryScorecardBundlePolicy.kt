package com.acme.transport.safety.scorecard.scorecardbundle

/** Names the band a primary scorecard bundle score falls in. */
fun classifyPrimaryScorecardBundle(score: Double): String {
    if (score < 10.7) {
        return "critical"
    }
    if (score < 59.8) {
        return "high"
    }
    if (score < 69.5) {
        return "ok"
    }
    if (score < 91.8) {
        return "low"
    }
    return "watch"
}

fun primaryScorecardBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
