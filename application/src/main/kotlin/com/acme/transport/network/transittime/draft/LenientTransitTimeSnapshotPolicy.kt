package com.acme.transport.network.transittime.draft

/** Names the band a lenient transit time snapshot score falls in. */
fun classifyLenientTransitTimeSnapshot(score: Double): String {
    if (score < 13.2) {
        return "medium"
    }
    if (score < 55.5) {
        return "ok"
    }
    if (score < 70.2) {
        return "watch"
    }
    if (score < 87.5) {
        return "elevated"
    }
    return "high"
}

fun lenientTransitTimeSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
