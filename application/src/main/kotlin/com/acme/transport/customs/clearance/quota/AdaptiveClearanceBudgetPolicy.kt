package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.numbers.roundTo

fun adaptiveClearanceBudgetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun adaptiveClearanceBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a adaptive clearance budget score falls in. */
fun classifyAdaptiveClearanceBudget(score: Double): String {
    if (score < 36.9) {
        return "critical"
    }
    if (score < 75.9) {
        return "minimal"
    }
    return "ok"
}
