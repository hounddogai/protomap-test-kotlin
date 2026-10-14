package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun appointmentVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAppointmentVariance(): Int {
    val policy = defaultPolicy("appointment-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a appointment variance score falls in. */
fun classifyAppointmentVariance(score: Double): String {
    if (score < 1.6) {
        return "critical"
    }
    if (score < 80.4) {
        return "low"
    }
    if (score < 88.7) {
        return "medium"
    }
    return "watch"
}
