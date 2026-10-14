package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a rolling dock door tier code in its canonical RDDT- form. */
fun normalizeRollingDockDoorTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDDT-")) {
        return clean
    }
    return "RDDT-${clean.replace(" ", "-")}"
}

fun rollingDockDoorTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun rollingDockDoorTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
