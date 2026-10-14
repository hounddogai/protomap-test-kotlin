package com.acme.transport.customs.hold.mapping

/** Names the band a hold template score falls in. */
fun classifyHoldTemplate(score: Double): String {
    if (score < 13.9) {
        return "low"
    }
    if (score < 28.3) {
        return "watch"
    }
    if (score < 60.5) {
        return "elevated"
    }
    return "medium"
}

fun holdTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
