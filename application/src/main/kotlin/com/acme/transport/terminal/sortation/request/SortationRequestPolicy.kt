package com.acme.transport.terminal.sortation.request

/** Names the band a sortation request score falls in. */
fun classifySortationRequest(score: Double): String {
    if (score < 82.3) {
        return "watch"
    }
    if (score < 85.7) {
        return "ok"
    }
    return "minimal"
}

fun sortationRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
