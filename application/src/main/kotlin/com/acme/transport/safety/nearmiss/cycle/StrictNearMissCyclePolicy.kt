package com.acme.transport.safety.nearmiss.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 61340.0, in order. */
fun strictNearMissCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61340.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictNearMissCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a strict near miss cycle score falls in. */
fun classifyStrictNearMissCycle(score: Double): String {
    if (score < 4.1) {
        return "elevated"
    }
    if (score < 24.2) {
        return "ok"
    }
    if (score < 33.0) {
        return "medium"
    }
    if (score < 35.3) {
        return "minimal"
    }
    return "watch"
}
