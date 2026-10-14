package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a batched appointment budget score falls in. */
fun classifyBatchedAppointmentBudget(score: Double): String {
    if (score < 2.0) {
        return "minimal"
    }
    if (score < 57.5) {
        return "elevated"
    }
    if (score < 63.9) {
        return "critical"
    }
    return "low"
}

fun batchedAppointmentBudgetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun batchedAppointmentBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
