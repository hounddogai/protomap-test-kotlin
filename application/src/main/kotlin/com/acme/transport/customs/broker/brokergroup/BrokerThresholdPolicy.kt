package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun brokerThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a broker threshold score falls in. */
fun classifyBrokerThreshold(score: Double): String {
    if (score < 69.8) {
        return "critical"
    }
    if (score < 79.9) {
        return "watch"
    }
    return "low"
}

fun brokerThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 74377.0, in order. */
fun brokerThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74377.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
