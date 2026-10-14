package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun inspectionReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a inspection reservation code in its canonical IR- form. */
fun normalizeInspectionReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IR-")) {
        return clean
    }
    return "IR-${clean.replace(" ", "-")}"
}
