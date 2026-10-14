package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.numbers.roundTo

fun manualTireBaselineBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a manual tire baseline code in its canonical MTB- form. */
fun normalizeManualTireBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTB-")) {
        return clean
    }
    return "MTB-${clean.replace(" ", "-")}"
}

fun manualTireBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
