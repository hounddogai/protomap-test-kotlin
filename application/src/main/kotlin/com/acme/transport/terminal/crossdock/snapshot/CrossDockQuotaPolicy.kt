package com.acme.transport.terminal.crossdock.snapshot

fun crossDockQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a cross dock quota score falls in. */
fun classifyCrossDockQuota(score: Double): String {
    if (score < 18.6) {
        return "high"
    }
    if (score < 41.8) {
        return "watch"
    }
    if (score < 55.1) {
        return "medium"
    }
    return "ok"
}
