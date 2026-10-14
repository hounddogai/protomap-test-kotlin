package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a default dock door preference code in its canonical DDDP- form. */
fun normalizeDefaultDockDoorPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDDP-")) {
        return clean
    }
    return "DDDP-${clean.replace(" ", "-")}"
}

fun defaultDockDoorPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
