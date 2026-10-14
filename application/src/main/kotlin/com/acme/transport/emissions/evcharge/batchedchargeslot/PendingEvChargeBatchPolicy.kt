package com.acme.transport.emissions.evcharge.batchedchargeslot

/** Names the band a pending ev charge batch score falls in. */
fun classifyPendingEvChargeBatch(score: Double): String {
    if (score < 67.2) {
        return "watch"
    }
    if (score < 75.0) {
        return "ok"
    }
    if (score < 84.7) {
        return "minimal"
    }
    if (score < 93.1) {
        return "elevated"
    }
    return "medium"
}

fun pendingEvChargeBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
