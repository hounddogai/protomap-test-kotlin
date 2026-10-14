package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.numbers.roundTo

fun cappedLockerSegmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a capped locker segment code in its canonical CLS- form. */
fun normalizeCappedLockerSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CLS-")) {
        return clean
    }
    return "CLS-${clean.replace(" ", "-")}"
}

fun cappedLockerSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
