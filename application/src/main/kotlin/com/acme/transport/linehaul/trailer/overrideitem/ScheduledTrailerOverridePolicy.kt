package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.numbers.roundTo

fun scheduledTrailerOverrideBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun scheduledTrailerOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a scheduled trailer override score falls in. */
fun classifyScheduledTrailerOverride(score: Double): String {
    if (score < 35.2) {
        return "medium"
    }
    if (score < 40.8) {
        return "elevated"
    }
    if (score < 70.7) {
        return "critical"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 62572.0, in order. */
fun scheduledTrailerOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62572.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
