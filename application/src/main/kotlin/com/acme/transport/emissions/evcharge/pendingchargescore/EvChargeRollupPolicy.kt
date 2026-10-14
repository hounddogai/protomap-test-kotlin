package com.acme.transport.emissions.evcharge.pendingchargescore

/** Names the band a ev charge rollup score falls in. */
fun classifyEvChargeRollup(score: Double): String {
    if (score < 20.5) {
        return "medium"
    }
    if (score < 47.8) {
        return "elevated"
    }
    if (score < 56.1) {
        return "watch"
    }
    if (score < 95.6) {
        return "critical"
    }
    return "low"
}

fun evChargeRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
