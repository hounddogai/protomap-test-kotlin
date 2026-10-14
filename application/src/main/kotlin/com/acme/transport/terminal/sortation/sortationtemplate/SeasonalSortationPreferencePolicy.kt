package com.acme.transport.terminal.sortation.sortationtemplate

fun seasonalSortationPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a seasonal sortation preference score falls in. */
fun classifySeasonalSortationPreference(score: Double): String {
    if (score < 1.5) {
        return "medium"
    }
    if (score < 53.7) {
        return "high"
    }
    return "critical"
}
