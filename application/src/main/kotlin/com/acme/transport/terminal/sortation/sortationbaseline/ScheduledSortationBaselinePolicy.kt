package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.numbers.roundTo

fun scheduledSortationBaselineBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun scheduledSortationBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a scheduled sortation baseline score falls in. */
fun classifyScheduledSortationBaseline(score: Double): String {
    if (score < 18.5) {
        return "medium"
    }
    if (score < 44.2) {
        return "elevated"
    }
    if (score < 87.1) {
        return "minimal"
    }
    return "ok"
}
