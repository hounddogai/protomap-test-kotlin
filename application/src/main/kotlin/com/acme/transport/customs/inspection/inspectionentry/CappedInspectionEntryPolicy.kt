package com.acme.transport.customs.inspection.inspectionentry

/** Names the band a capped inspection entry score falls in. */
fun classifyCappedInspectionEntry(score: Double): String {
    if (score < 16.2) {
        return "medium"
    }
    if (score < 94.5) {
        return "elevated"
    }
    return "critical"
}

/** Puts a capped inspection entry code in its canonical CIE- form. */
fun normalizeCappedInspectionEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CIE-")) {
        return clean
    }
    return "CIE-${clean.replace(" ", "-")}"
}
