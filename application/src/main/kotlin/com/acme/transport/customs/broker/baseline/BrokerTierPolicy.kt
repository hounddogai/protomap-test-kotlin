package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun brokerTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 78425.0, in order. */
fun brokerTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78425.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a broker tier score falls in. */
fun classifyBrokerTier(score: Double): String {
    if (score < 14.1) {
        return "critical"
    }
    if (score < 31.8) {
        return "low"
    }
    if (score < 40.2) {
        return "ok"
    }
    return "high"
}
