package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingScorecardGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a rolling scorecard group code in its canonical RSG- form. */
fun normalizeRollingScorecardGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RSG-")) {
        return clean
    }
    return "RSG-${clean.replace(" ", "-")}"
}
