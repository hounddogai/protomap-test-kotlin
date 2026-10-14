package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 79516.0, in order. */
fun primaryCameraEventTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79516.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryCameraEventTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary camera event target score falls in. */
fun classifyPrimaryCameraEventTarget(score: Double): String {
    if (score < 4.0) {
        return "low"
    }
    if (score < 14.6) {
        return "minimal"
    }
    return "elevated"
}
