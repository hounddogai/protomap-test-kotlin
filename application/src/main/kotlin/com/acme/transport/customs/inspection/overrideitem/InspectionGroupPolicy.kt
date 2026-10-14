package com.acme.transport.customs.inspection.overrideitem

fun inspectionGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a inspection group code in its canonical IG- form. */
fun normalizeInspectionGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IG-")) {
        return clean
    }
    return "IG-${clean.replace(" ", "-")}"
}
