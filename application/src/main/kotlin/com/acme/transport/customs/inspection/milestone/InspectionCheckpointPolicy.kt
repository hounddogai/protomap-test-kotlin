package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.numbers.roundTo

/** Puts a inspection checkpoint code in its canonical IC- form. */
fun normalizeInspectionCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IC-")) {
        return clean
    }
    return "IC-${clean.replace(" ", "-")}"
}

fun inspectionCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun inspectionCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
