package com.acme.transport.customs.clearance.segment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun clearanceMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 77473.0, in order. */
fun clearanceMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77473.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun clearanceMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a clearance milestone score falls in. */
fun classifyClearanceMilestone(score: Double): String {
    if (score < 19.7) {
        return "high"
    }
    if (score < 44.0) {
        return "minimal"
    }
    if (score < 91.8) {
        return "critical"
    }
    return "low"
}
