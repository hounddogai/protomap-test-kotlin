package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a pending accessorial group score falls in. */
fun classifyPendingAccessorialGroup(score: Double): String {
    if (score < 61.2) {
        return "minimal"
    }
    if (score < 98.5) {
        return "watch"
    }
    return "high"
}

fun pendingAccessorialGroupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun pendingAccessorialGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 18508.0, in order. */
fun pendingAccessorialGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 18508.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
