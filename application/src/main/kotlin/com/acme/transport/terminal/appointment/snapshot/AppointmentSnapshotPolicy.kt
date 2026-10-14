package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun appointmentSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a appointment snapshot code in its canonical AS- form. */
fun normalizeAppointmentSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AS-")) {
        return clean
    }
    return "AS-${clean.replace(" ", "-")}"
}

/** Names the band a appointment snapshot score falls in. */
fun classifyAppointmentSnapshot(score: Double): String {
    if (score < 12.4) {
        return "low"
    }
    if (score < 20.6) {
        return "high"
    }
    if (score < 38.6) {
        return "medium"
    }
    return "minimal"
}
