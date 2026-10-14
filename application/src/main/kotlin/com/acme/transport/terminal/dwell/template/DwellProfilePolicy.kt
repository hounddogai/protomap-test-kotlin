package com.acme.transport.terminal.dwell.template

/** Names the band a dwell profile score falls in. */
fun classifyDwellProfile(score: Double): String {
    if (score < 8.1) {
        return "medium"
    }
    if (score < 28.7) {
        return "ok"
    }
    if (score < 63.5) {
        return "minimal"
    }
    return "elevated"
}

fun dwellProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
