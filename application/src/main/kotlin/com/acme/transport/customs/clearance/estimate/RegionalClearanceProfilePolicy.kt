package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a regional clearance profile code in its canonical RCP- form. */
fun normalizeRegionalClearanceProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCP-")) {
        return clean
    }
    return "RCP-${clean.replace(" ", "-")}"
}

fun regionalClearanceProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
