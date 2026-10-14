package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a rolling incident baseline code in its canonical RIB- form. */
fun normalizeRollingIncidentBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RIB-")) {
        return clean
    }
    return "RIB-${clean.replace(" ", "-")}"
}

fun rollingIncidentBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
