package com.acme.transport.safety.incident.incidentmanifest

fun incidentDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Puts a incident draft code in its canonical ID- form. */
fun normalizeIncidentDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ID-")) {
        return clean
    }
    return "ID-${clean.replace(" ", "-")}"
}
