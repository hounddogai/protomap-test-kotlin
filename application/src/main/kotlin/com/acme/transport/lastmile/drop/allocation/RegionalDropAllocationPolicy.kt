package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.numbers.roundTo

fun regionalDropAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a regional drop allocation code in its canonical RDA- form. */
fun normalizeRegionalDropAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDA-")) {
        return clean
    }
    return "RDA-${clean.replace(" ", "-")}"
}

fun regionalDropAllocationBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
