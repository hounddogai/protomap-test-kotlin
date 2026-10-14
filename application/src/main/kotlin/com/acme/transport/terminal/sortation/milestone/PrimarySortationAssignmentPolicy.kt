package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.numbers.roundTo

fun primarySortationAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a primary sortation assignment code in its canonical PSA- form. */
fun normalizePrimarySortationAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSA-")) {
        return clean
    }
    return "PSA-${clean.replace(" ", "-")}"
}

fun primarySortationAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
