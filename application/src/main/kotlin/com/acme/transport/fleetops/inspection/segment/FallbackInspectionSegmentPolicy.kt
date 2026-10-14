package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.numbers.roundTo

fun fallbackInspectionSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a fallback inspection segment score falls in. */
fun classifyFallbackInspectionSegment(score: Double): String {
    if (score < 7.2) {
        return "critical"
    }
    if (score < 35.5) {
        return "high"
    }
    if (score < 70.1) {
        return "watch"
    }
    return "minimal"
}

fun fallbackInspectionSegmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 40854.0, in order. */
fun fallbackInspectionSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40854.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
