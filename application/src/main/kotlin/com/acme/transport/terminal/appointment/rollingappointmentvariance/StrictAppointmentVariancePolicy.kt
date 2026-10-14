package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.numbers.roundTo

/** Puts a strict appointment variance code in its canonical SAV- form. */
fun normalizeStrictAppointmentVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAV-")) {
        return clean
    }
    return "SAV-${clean.replace(" ", "-")}"
}

fun strictAppointmentVarianceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a strict appointment variance score falls in. */
fun classifyStrictAppointmentVariance(score: Double): String {
    if (score < 38.6) {
        return "low"
    }
    if (score < 43.5) {
        return "medium"
    }
    if (score < 88.8) {
        return "high"
    }
    return "ok"
}
