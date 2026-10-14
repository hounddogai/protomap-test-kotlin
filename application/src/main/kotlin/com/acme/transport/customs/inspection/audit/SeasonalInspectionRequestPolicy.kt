package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.numbers.roundTo

/** Puts a seasonal inspection request code in its canonical SIR- form. */
fun normalizeSeasonalInspectionRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SIR-")) {
        return clean
    }
    return "SIR-${clean.replace(" ", "-")}"
}

fun seasonalInspectionRequestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
