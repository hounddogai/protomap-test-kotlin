package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.numbers.roundTo

fun manualZoneAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun manualZoneAllocationBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a manual zone allocation code in its canonical MZA- form. */
fun normalizeManualZoneAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MZA-")) {
        return clean
    }
    return "MZA-${clean.replace(" ", "-")}"
}
