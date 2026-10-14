package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a batched appointment variance score falls in. */
fun classifyBatchedAppointmentVariance(score: Double): String {
    if (score < 22.7) {
        return "medium"
    }
    if (score < 34.8) {
        return "high"
    }
    if (score < 49.5) {
        return "watch"
    }
    if (score < 57.6) {
        return "low"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 41234.0, in order. */
fun batchedAppointmentVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41234.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedAppointmentVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
