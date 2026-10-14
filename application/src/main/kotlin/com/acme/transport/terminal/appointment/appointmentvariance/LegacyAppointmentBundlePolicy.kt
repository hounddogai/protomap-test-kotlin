package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 21528.0, in order. */
fun legacyAppointmentBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21528.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy appointment bundle code in its canonical LAB- form. */
fun normalizeLegacyAppointmentBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAB-")) {
        return clean
    }
    return "LAB-${clean.replace(" ", "-")}"
}

/** Names the band a legacy appointment bundle score falls in. */
fun classifyLegacyAppointmentBundle(score: Double): String {
    if (score < 8.0) {
        return "high"
    }
    if (score < 9.8) {
        return "critical"
    }
    if (score < 43.5) {
        return "medium"
    }
    if (score < 78.1) {
        return "minimal"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyAppointmentBundle(): Int {
    val policy = defaultPolicy("legacy-appointment-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
