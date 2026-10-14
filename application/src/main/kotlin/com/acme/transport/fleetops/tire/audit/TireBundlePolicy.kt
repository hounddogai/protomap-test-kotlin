package com.acme.transport.fleetops.tire.audit

import com.acme.transport.kit.numbers.roundTo

/** Names the band a tire bundle score falls in. */
fun classifyTireBundle(score: Double): String {
    if (score < 52.9) {
        return "watch"
    }
    if (score < 61.7) {
        return "low"
    }
    if (score < 78.7) {
        return "critical"
    }
    if (score < 99.6) {
        return "elevated"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 71083.0, in order. */
fun tireBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71083.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tireBundleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
