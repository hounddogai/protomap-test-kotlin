package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalAppointmentMilestone(): Int {
    val policy = defaultPolicy("regional-appointment-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a regional appointment milestone code in its canonical RAM- form. */
fun normalizeRegionalAppointmentMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAM-")) {
        return clean
    }
    return "RAM-${clean.replace(" ", "-")}"
}
