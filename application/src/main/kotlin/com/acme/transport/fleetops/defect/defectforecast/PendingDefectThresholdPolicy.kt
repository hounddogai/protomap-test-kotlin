package com.acme.transport.fleetops.defect.defectforecast

fun pendingDefectThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a pending defect threshold code in its canonical PDT- form. */
fun normalizePendingDefectThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDT-")) {
        return clean
    }
    return "PDT-${clean.replace(" ", "-")}"
}
