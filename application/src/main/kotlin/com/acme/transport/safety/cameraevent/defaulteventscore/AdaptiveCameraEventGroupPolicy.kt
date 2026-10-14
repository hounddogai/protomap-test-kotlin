package com.acme.transport.safety.cameraevent.defaulteventscore

/** Names the band a adaptive camera event group score falls in. */
fun classifyAdaptiveCameraEventGroup(score: Double): String {
    if (score < 27.4) {
        return "high"
    }
    if (score < 39.4) {
        return "critical"
    }
    return "elevated"
}

/** Puts a adaptive camera event group code in its canonical ACEG- form. */
fun normalizeAdaptiveCameraEventGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACEG-")) {
        return clean
    }
    return "ACEG-${clean.replace(" ", "-")}"
}

fun adaptiveCameraEventGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
