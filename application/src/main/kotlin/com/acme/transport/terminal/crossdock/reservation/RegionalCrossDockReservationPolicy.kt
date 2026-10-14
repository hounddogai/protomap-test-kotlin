package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalCrossDockReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a regional cross dock reservation code in its canonical RCDR- form. */
fun normalizeRegionalCrossDockReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCDR-")) {
        return clean
    }
    return "RCDR-${clean.replace(" ", "-")}"
}

fun regionalCrossDockReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
