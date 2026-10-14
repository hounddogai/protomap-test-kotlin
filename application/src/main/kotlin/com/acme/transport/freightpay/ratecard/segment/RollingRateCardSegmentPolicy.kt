package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 8141.0, in order. */
fun rollingRateCardSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8141.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rolling rate card segment score falls in. */
fun classifyRollingRateCardSegment(score: Double): String {
    if (score < 7.4) {
        return "minimal"
    }
    if (score < 15.9) {
        return "medium"
    }
    if (score < 82.1) {
        return "low"
    }
    return "high"
}

fun rollingRateCardSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
