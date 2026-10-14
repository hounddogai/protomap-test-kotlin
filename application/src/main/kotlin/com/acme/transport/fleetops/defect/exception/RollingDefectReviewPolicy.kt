package com.acme.transport.fleetops.defect.exception

fun rollingDefectReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a rolling defect review code in its canonical RDR- form. */
fun normalizeRollingDefectReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDR-")) {
        return clean
    }
    return "RDR-${clean.replace(" ", "-")}"
}
