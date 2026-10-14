package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.retry.defaultPolicy

fun appointmentOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a appointment override code in its canonical AO- form. */
fun normalizeAppointmentOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AO-")) {
        return clean
    }
    return "AO-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAppointmentOverride(): Int {
    val policy = defaultPolicy("appointment-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
