package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.kit.numbers.roundTo

fun weightedAccessorialAllocationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun weightedAccessorialAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a weighted accessorial allocation code in its canonical WAA- form. */
fun normalizeWeightedAccessorialAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WAA-")) {
        return clean
    }
    return "WAA-${clean.replace(" ", "-")}"
}
