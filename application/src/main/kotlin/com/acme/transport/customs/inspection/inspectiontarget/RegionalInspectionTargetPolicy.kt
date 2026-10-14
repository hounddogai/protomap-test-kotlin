package com.acme.transport.customs.inspection.inspectiontarget

fun regionalInspectionTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Puts a regional inspection target code in its canonical RIT- form. */
fun normalizeRegionalInspectionTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RIT-")) {
        return clean
    }
    return "RIT-${clean.replace(" ", "-")}"
}
