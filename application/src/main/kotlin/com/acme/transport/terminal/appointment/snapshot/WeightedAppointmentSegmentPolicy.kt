package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a weighted appointment segment code in its canonical WAS- form. */
fun normalizeWeightedAppointmentSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WAS-")) {
        return clean
    }
    return "WAS-${clean.replace(" ", "-")}"
}

fun weightedAppointmentSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
