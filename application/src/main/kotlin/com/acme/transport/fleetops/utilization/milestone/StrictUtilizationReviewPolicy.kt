package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a strict utilization review score falls in. */
fun classifyStrictUtilizationReview(score: Double): String {
    if (score < 54.3) {
        return "ok"
    }
    if (score < 66.0) {
        return "minimal"
    }
    if (score < 80.9) {
        return "low"
    }
    if (score < 88.3) {
        return "high"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 74656.0, in order. */
fun strictUtilizationReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74656.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictUtilizationReviewBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun strictUtilizationReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
