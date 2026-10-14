package com.acme.transport.linehaul.tender.summary

/** Names the band a tender request score falls in. */
fun classifyTenderRequest(score: Double): String {
    if (score < 10.4) {
        return "minimal"
    }
    if (score < 27.3) {
        return "elevated"
    }
    if (score < 35.9) {
        return "ok"
    }
    if (score < 40.4) {
        return "medium"
    }
    return "watch"
}

fun tenderRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
