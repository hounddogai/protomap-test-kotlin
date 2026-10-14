package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 33474.0, in order. */
fun relayReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33474.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun relayReviewBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun relayReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
