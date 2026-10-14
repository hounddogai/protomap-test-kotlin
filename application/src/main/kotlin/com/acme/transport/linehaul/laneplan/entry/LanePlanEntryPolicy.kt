package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.numbers.roundTo

fun lanePlanEntryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a lane plan entry code in its canonical LPE- form. */
fun normalizeLanePlanEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPE-")) {
        return clean
    }
    return "LPE-${clean.replace(" ", "-")}"
}

fun lanePlanEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
