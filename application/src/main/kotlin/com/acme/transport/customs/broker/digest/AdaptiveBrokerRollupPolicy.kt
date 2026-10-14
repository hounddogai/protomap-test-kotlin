package com.acme.transport.customs.broker.digest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun adaptiveBrokerRollupBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 88554.0, in order. */
fun adaptiveBrokerRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88554.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a adaptive broker rollup score falls in. */
fun classifyAdaptiveBrokerRollup(score: Double): String {
    if (score < 47.2) {
        return "medium"
    }
    if (score < 72.7) {
        return "elevated"
    }
    return "high"
}

fun adaptiveBrokerRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.22
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
