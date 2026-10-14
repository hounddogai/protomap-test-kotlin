package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.numbers.roundTo

fun defectGroupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a defect group code in its canonical DG- form. */
fun normalizeDefectGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DG-")) {
        return clean
    }
    return "DG-${clean.replace(" ", "-")}"
}

fun defectGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
