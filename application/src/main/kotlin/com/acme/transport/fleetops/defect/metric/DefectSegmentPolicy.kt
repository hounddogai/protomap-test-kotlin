package com.acme.transport.fleetops.defect.metric

import com.acme.transport.kit.numbers.roundTo

fun defectSegmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a defect segment code in its canonical DS- form. */
fun normalizeDefectSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DS-")) {
        return clean
    }
    return "DS-${clean.replace(" ", "-")}"
}
