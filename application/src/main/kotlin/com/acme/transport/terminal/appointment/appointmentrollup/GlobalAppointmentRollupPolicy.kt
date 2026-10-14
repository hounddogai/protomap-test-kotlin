package com.acme.transport.terminal.appointment.appointmentrollup

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalAppointmentRollup(): Int {
    val policy = defaultPolicy("global-appointment-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global appointment rollup score falls in. */
fun classifyGlobalAppointmentRollup(score: Double): String {
    if (score < 40.9) {
        return "minimal"
    }
    if (score < 66.6) {
        return "low"
    }
    if (score < 87.2) {
        return "ok"
    }
    if (score < 96.8) {
        return "watch"
    }
    return "elevated"
}

fun globalAppointmentRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
