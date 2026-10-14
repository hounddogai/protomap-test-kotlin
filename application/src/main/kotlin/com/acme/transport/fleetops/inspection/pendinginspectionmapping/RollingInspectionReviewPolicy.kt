package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingInspectionReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a rolling inspection review code in its canonical RIR- form. */
fun normalizeRollingInspectionReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RIR-")) {
        return clean
    }
    return "RIR-${clean.replace(" ", "-")}"
}
