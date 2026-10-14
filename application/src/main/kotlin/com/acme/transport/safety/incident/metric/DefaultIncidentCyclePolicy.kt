package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultIncidentCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a default incident cycle code in its canonical DIC- form. */
fun normalizeDefaultIncidentCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DIC-")) {
        return clean
    }
    return "DIC-${clean.replace(" ", "-")}"
}

fun defaultIncidentCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.14
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
