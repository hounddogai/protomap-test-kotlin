package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.numbers.roundTo

/** Puts a inspection agreement code in its canonical IA- form. */
fun normalizeInspectionAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IA-")) {
        return clean
    }
    return "IA-${clean.replace(" ", "-")}"
}

fun inspectionAgreementBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
