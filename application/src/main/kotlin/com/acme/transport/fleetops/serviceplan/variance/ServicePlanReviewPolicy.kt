package com.acme.transport.fleetops.serviceplan.variance

fun servicePlanReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Puts a service plan review code in its canonical SPR- form. */
fun normalizeServicePlanReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPR-")) {
        return clean
    }
    return "SPR-${clean.replace(" ", "-")}"
}
