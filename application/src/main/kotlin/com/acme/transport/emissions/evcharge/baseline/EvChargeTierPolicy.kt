package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 70204.0, in order. */
fun evChargeTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70204.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun evChargeTierBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a ev charge tier score falls in. */
fun classifyEvChargeTier(score: Double): String {
    if (score < 7.6) {
        return "high"
    }
    if (score < 36.2) {
        return "low"
    }
    return "minimal"
}

fun evChargeTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
