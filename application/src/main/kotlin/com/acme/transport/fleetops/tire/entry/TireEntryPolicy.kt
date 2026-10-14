package com.acme.transport.fleetops.tire.entry

/** Names the band a tire entry score falls in. */
fun classifyTireEntry(score: Double): String {
    if (score < 24.9) {
        return "minimal"
    }
    if (score < 52.6) {
        return "high"
    }
    return "ok"
}

fun tireEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
