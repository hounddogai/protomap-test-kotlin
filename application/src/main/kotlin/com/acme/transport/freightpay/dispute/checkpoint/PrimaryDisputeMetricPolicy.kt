package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a primary dispute metric score falls in. */
fun classifyPrimaryDisputeMetric(score: Double): String {
    if (score < 19.6) {
        return "elevated"
    }
    if (score < 31.7) {
        return "minimal"
    }
    return "high"
}

fun primaryDisputeMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 11557.0, in order. */
fun primaryDisputeMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11557.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryDisputeMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
