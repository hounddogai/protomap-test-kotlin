package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.numbers.roundTo

fun holdWindowBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun holdWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Puts a hold window code in its canonical HW- form. */
fun normalizeHoldWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HW-")) {
        return clean
    }
    return "HW-${clean.replace(" ", "-")}"
}
