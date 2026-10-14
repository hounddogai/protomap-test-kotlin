package com.acme.transport.customs.hold.forecast

/** Names the band a scheduled hold forecast score falls in. */
fun classifyScheduledHoldForecast(score: Double): String {
    if (score < 1.7) {
        return "high"
    }
    if (score < 41.8) {
        return "watch"
    }
    if (score < 52.9) {
        return "ok"
    }
    if (score < 72.5) {
        return "low"
    }
    return "medium"
}

fun scheduledHoldForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
