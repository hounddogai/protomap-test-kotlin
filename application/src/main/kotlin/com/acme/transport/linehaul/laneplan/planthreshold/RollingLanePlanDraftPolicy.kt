package com.acme.transport.linehaul.laneplan.planthreshold

fun rollingLanePlanDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a rolling lane plan draft score falls in. */
fun classifyRollingLanePlanDraft(score: Double): String {
    if (score < 62.5) {
        return "critical"
    }
    if (score < 99.9) {
        return "minimal"
    }
    return "low"
}
