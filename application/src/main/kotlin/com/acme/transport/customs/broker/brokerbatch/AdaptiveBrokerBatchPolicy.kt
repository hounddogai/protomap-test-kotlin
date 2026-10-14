package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveBrokerBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.52
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive broker batch score falls in. */
fun classifyAdaptiveBrokerBatch(score: Double): String {
    if (score < 25.6) {
        return "ok"
    }
    if (score < 36.3) {
        return "critical"
    }
    if (score < 40.1) {
        return "minimal"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 69065.0, in order. */
fun adaptiveBrokerBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69065.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
