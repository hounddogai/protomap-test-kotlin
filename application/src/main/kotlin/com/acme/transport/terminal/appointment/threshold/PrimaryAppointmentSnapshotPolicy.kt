package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.numbers.roundTo

/** Puts a primary appointment snapshot code in its canonical PAS- form. */
fun normalizePrimaryAppointmentSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAS-")) {
        return clean
    }
    return "PAS-${clean.replace(" ", "-")}"
}

fun primaryAppointmentSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun primaryAppointmentSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a primary appointment snapshot score falls in. */
fun classifyPrimaryAppointmentSnapshot(score: Double): String {
    if (score < 27.2) {
        return "watch"
    }
    if (score < 54.8) {
        return "medium"
    }
    return "minimal"
}
