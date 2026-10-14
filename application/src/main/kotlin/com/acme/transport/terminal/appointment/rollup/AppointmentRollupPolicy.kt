package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun appointmentRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAppointmentRollup(): Int {
    val policy = defaultPolicy("appointment-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a appointment rollup score falls in. */
fun classifyAppointmentRollup(score: Double): String {
    if (score < 30.6) {
        return "minimal"
    }
    if (score < 42.4) {
        return "medium"
    }
    if (score < 44.2) {
        return "critical"
    }
    if (score < 56.6) {
        return "low"
    }
    return "high"
}
