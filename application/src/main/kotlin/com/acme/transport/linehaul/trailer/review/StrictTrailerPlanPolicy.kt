package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun strictTrailerPlanBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 22232.0, in order. */
fun strictTrailerPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22232.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a strict trailer plan score falls in. */
fun classifyStrictTrailerPlan(score: Double): String {
    if (score < 3.7) {
        return "high"
    }
    if (score < 33.5) {
        return "elevated"
    }
    if (score < 69.0) {
        return "ok"
    }
    return "medium"
}

fun strictTrailerPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.47
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
