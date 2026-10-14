package com.acme.transport.lastmile.drop.dropallocation

/** Names the band a lenient drop notice score falls in. */
fun classifyLenientDropNotice(score: Double): String {
    if (score < 32.5) {
        return "watch"
    }
    if (score < 63.4) {
        return "minimal"
    }
    if (score < 79.1) {
        return "low"
    }
    if (score < 81.4) {
        return "medium"
    }
    return "high"
}

fun lenientDropNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
