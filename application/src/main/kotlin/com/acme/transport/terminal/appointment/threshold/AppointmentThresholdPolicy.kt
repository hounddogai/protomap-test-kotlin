package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a appointment threshold score falls in. */
fun classifyAppointmentThreshold(score: Double): String {
    if (score < 42.3) {
        return "low"
    }
    if (score < 50.5) {
        return "ok"
    }
    if (score < 78.9) {
        return "watch"
    }
    return "minimal"
}

/** Puts a appointment threshold code in its canonical AT- form. */
fun normalizeAppointmentThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AT-")) {
        return clean
    }
    return "AT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAppointmentThreshold(): Int {
    val policy = defaultPolicy("appointment-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
