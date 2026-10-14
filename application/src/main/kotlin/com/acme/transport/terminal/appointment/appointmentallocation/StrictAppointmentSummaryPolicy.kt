package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictAppointmentSummary(): Int {
    val policy = defaultPolicy("strict-appointment-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun strictAppointmentSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.83
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
