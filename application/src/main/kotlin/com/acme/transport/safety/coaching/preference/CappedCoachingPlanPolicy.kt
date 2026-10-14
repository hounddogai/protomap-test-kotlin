package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 45208.0, in order. */
fun cappedCoachingPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45208.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a capped coaching plan score falls in. */
fun classifyCappedCoachingPlan(score: Double): String {
    if (score < 13.0) {
        return "low"
    }
    if (score < 39.6) {
        return "watch"
    }
    if (score < 94.3) {
        return "ok"
    }
    return "high"
}

fun cappedCoachingPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
