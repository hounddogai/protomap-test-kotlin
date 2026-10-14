package com.acme.transport.safety.violation.threshold

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun violationBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 99854.0, in order. */
fun violationBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99854.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a violation batch score falls in. */
fun classifyViolationBatch(score: Double): String {
    if (score < 31.7) {
        return "high"
    }
    if (score < 34.9) {
        return "ok"
    }
    return "low"
}
