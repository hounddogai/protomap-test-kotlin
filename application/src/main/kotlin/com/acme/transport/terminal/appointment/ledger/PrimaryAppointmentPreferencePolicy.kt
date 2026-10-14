package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryAppointmentPreference(): Int {
    val policy = defaultPolicy("primary-appointment-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a primary appointment preference code in its canonical PAP- form. */
fun normalizePrimaryAppointmentPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAP-")) {
        return clean
    }
    return "PAP-${clean.replace(" ", "-")}"
}
