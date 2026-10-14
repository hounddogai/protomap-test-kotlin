package com.acme.transport.safety.incident.incidentreservation

fun batchedIncidentPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Puts a batched incident plan code in its canonical BIP- form. */
fun normalizeBatchedIncidentPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BIP-")) {
        return clean
    }
    return "BIP-${clean.replace(" ", "-")}"
}
