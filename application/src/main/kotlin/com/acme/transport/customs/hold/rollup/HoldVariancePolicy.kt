package com.acme.transport.customs.hold.rollup

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 83344.0, in order. */
fun holdVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83344.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a hold variance score falls in. */
fun classifyHoldVariance(score: Double): String {
    if (score < 3.8) {
        return "high"
    }
    if (score < 91.5) {
        return "low"
    }
    return "medium"
}

fun holdVarianceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
