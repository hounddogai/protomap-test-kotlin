package com.acme.transport.linehaul.laneplan.bundle

fun fallbackLanePlanSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a fallback lane plan summary score falls in. */
fun classifyFallbackLanePlanSummary(score: Double): String {
    if (score < 29.5) {
        return "minimal"
    }
    if (score < 71.2) {
        return "low"
    }
    return "ok"
}
