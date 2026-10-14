package com.acme.transport.customs.clearance.seasonalclearanceoverride

fun globalClearanceOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a global clearance override score falls in. */
fun classifyGlobalClearanceOverride(score: Double): String {
    if (score < 15.0) {
        return "critical"
    }
    if (score < 54.7) {
        return "elevated"
    }
    return "watch"
}
