package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.numbers.roundTo

fun scheduledTransitTimeEntryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a scheduled transit time entry score falls in. */
fun classifyScheduledTransitTimeEntry(score: Double): String {
    if (score < 54.0) {
        return "high"
    }
    if (score < 85.0) {
        return "watch"
    }
    return "critical"
}
