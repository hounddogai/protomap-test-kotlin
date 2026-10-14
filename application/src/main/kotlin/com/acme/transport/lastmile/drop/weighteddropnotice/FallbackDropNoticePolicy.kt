package com.acme.transport.lastmile.drop.weighteddropnotice

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 75466.0, in order. */
fun fallbackDropNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75466.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a fallback drop notice score falls in. */
fun classifyFallbackDropNotice(score: Double): String {
    if (score < 50.5) {
        return "minimal"
    }
    if (score < 55.1) {
        return "medium"
    }
    if (score < 94.0) {
        return "elevated"
    }
    if (score < 97.9) {
        return "high"
    }
    return "low"
}

fun fallbackDropNoticeBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
