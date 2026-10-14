package com.acme.transport.network.zone.batch

fun zoneBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a zone batch score falls in. */
fun classifyZoneBatch(score: Double): String {
    if (score < 60.2) {
        return "high"
    }
    if (score < 92.0) {
        return "elevated"
    }
    if (score < 97.0) {
        return "low"
    }
    return "watch"
}
