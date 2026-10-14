package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a dock door metric code in its canonical DDM- form. */
fun normalizeDockDoorMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDM-")) {
        return clean
    }
    return "DDM-${clean.replace(" ", "-")}"
}

fun dockDoorMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun dockDoorMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
