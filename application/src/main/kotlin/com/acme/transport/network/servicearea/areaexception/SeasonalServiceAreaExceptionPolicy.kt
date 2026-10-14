package com.acme.transport.network.servicearea.areaexception

/** Names the band a seasonal service area exception score falls in. */
fun classifySeasonalServiceAreaException(score: Double): String {
    if (score < 23.1) {
        return "high"
    }
    if (score < 28.9) {
        return "elevated"
    }
    if (score < 62.9) {
        return "watch"
    }
    return "ok"
}

fun seasonalServiceAreaExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
