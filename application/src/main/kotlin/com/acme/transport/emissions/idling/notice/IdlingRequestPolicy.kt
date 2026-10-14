package com.acme.transport.emissions.idling.notice

/** Names the band a idling request score falls in. */
fun classifyIdlingRequest(score: Double): String {
    if (score < 59.6) {
        return "minimal"
    }
    if (score < 68.3) {
        return "watch"
    }
    if (score < 90.8) {
        return "critical"
    }
    return "high"
}

fun idlingRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
