package com.acme.transport.customs.inspection.mapping

fun primaryInspectionProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a primary inspection profile score falls in. */
fun classifyPrimaryInspectionProfile(score: Double): String {
    if (score < 17.4) {
        return "critical"
    }
    if (score < 61.6) {
        return "high"
    }
    if (score < 68.4) {
        return "ok"
    }
    return "watch"
}

/** Puts a primary inspection profile code in its canonical PIP- form. */
fun normalizePrimaryInspectionProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PIP-")) {
        return clean
    }
    return "PIP-${clean.replace(" ", "-")}"
}
