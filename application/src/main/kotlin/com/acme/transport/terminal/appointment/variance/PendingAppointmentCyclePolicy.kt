package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun pendingAppointmentCycleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingAppointmentCycle(): Int {
    val policy = defaultPolicy("pending-appointment-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a pending appointment cycle code in its canonical PAC- form. */
fun normalizePendingAppointmentCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAC-")) {
        return clean
    }
    return "PAC-${clean.replace(" ", "-")}"
}

fun pendingAppointmentCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
