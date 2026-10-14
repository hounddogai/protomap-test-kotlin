package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tieredLockerMetricBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a tiered locker metric score falls in. */
fun classifyTieredLockerMetric(score: Double): String {
    if (score < 22.6) {
        return "medium"
    }
    if (score < 51.0) {
        return "high"
    }
    if (score < 75.9) {
        return "ok"
    }
    if (score < 91.9) {
        return "minimal"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 55404.0, in order. */
fun tieredLockerMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55404.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredLockerMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
