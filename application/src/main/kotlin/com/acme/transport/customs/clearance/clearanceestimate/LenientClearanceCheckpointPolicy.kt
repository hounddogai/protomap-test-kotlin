package com.acme.transport.customs.clearance.clearanceestimate

/** Puts a lenient clearance checkpoint code in its canonical LCC- form. */
fun normalizeLenientClearanceCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCC-")) {
        return clean
    }
    return "LCC-${clean.replace(" ", "-")}"
}

fun lenientClearanceCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
