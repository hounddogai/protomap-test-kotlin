package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.numbers.roundTo

/** Puts a inspection batch code in its canonical IB- form. */
fun normalizeInspectionBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IB-")) {
        return clean
    }
    return "IB-${clean.replace(" ", "-")}"
}

fun inspectionBatchBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
