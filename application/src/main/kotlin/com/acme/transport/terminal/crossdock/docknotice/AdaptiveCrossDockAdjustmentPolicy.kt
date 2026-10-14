package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.kit.numbers.roundTo

fun adaptiveCrossDockAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a adaptive cross dock adjustment code in its canonical ACDA- form. */
fun normalizeAdaptiveCrossDockAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACDA-")) {
        return clean
    }
    return "ACDA-${clean.replace(" ", "-")}"
}

fun adaptiveCrossDockAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
