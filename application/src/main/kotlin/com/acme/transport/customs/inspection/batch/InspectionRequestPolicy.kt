package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.numbers.roundTo

fun inspectionRequestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a inspection request code in its canonical IR- form. */
fun normalizeInspectionRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IR-")) {
        return clean
    }
    return "IR-${clean.replace(" ", "-")}"
}
