package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.numbers.roundTo

fun inspectionAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun inspectionAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a inspection adjustment code in its canonical IA- form. */
fun normalizeInspectionAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IA-")) {
        return clean
    }
    return "IA-${clean.replace(" ", "-")}"
}
