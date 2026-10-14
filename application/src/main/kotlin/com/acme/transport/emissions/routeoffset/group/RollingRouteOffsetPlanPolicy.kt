package com.acme.transport.emissions.routeoffset.group

/** Puts a rolling route offset plan code in its canonical RROP- form. */
fun normalizeRollingRouteOffsetPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RROP-")) {
        return clean
    }
    return "RROP-${clean.replace(" ", "-")}"
}

fun rollingRouteOffsetPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
