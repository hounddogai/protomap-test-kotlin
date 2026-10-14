package com.acme.transport.emissions.idling.proposal

fun rollingIdlingSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a rolling idling segment score falls in. */
fun classifyRollingIdlingSegment(score: Double): String {
    if (score < 5.5) {
        return "high"
    }
    if (score < 47.6) {
        return "watch"
    }
    return "medium"
}
