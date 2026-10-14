package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun crossDockAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun crossDockAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a cross dock allocation code in its canonical CDA- form. */
fun normalizeCrossDockAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDA-")) {
        return clean
    }
    return "CDA-${clean.replace(" ", "-")}"
}

fun crossDockAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
