package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 52295.0, in order. */
fun adaptiveDwellEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 52295.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveDwellEstimateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a adaptive dwell estimate score falls in. */
fun classifyAdaptiveDwellEstimate(score: Double): String {
    if (score < 30.9) {
        return "elevated"
    }
    if (score < 49.8) {
        return "minimal"
    }
    if (score < 57.3) {
        return "ok"
    }
    if (score < 99.0) {
        return "medium"
    }
    return "critical"
}
