package com.acme.transport.customs.hold.bundle

fun holdDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a hold draft score falls in. */
fun classifyHoldDraft(score: Double): String {
    if (score < 56.2) {
        return "minimal"
    }
    if (score < 56.3) {
        return "medium"
    }
    if (score < 81.3) {
        return "low"
    }
    if (score < 97.6) {
        return "watch"
    }
    return "high"
}
