package com.acme.transport.safety.incident.request

import com.acme.transport.kit.numbers.roundTo

fun fallbackIncidentRequestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a fallback incident request code in its canonical FIR- form. */
fun normalizeFallbackIncidentRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIR-")) {
        return clean
    }
    return "FIR-${clean.replace(" ", "-")}"
}
