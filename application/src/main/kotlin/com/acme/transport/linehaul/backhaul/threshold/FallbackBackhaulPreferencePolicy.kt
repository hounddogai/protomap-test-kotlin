package com.acme.transport.linehaul.backhaul.threshold

fun fallbackBackhaulPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a fallback backhaul preference score falls in. */
fun classifyFallbackBackhaulPreference(score: Double): String {
    if (score < 18.7) {
        return "ok"
    }
    if (score < 83.4) {
        return "medium"
    }
    if (score < 89.5) {
        return "critical"
    }
    if (score < 96.6) {
        return "watch"
    }
    return "elevated"
}
