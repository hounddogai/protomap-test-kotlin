package com.acme.transport.freightpay.accessorial.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 80914.0, in order. */
fun rollingAccessorialAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80914.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingAccessorialAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rolling accessorial allocation score falls in. */
fun classifyRollingAccessorialAllocation(score: Double): String {
    if (score < 45.8) {
        return "watch"
    }
    if (score < 47.9) {
        return "low"
    }
    return "high"
}
