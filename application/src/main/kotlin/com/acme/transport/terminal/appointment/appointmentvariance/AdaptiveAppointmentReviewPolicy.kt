package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a adaptive appointment review score falls in. */
fun classifyAdaptiveAppointmentReview(score: Double): String {
    if (score < 6.9) {
        return "high"
    }
    if (score < 76.2) {
        return "medium"
    }
    return "elevated"
}

fun adaptiveAppointmentReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.93
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveAppointmentReview(): Int {
    val policy = defaultPolicy("adaptive-appointment-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a adaptive appointment review code in its canonical AAR- form. */
fun normalizeAdaptiveAppointmentReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AAR-")) {
        return clean
    }
    return "AAR-${clean.replace(" ", "-")}"
}
