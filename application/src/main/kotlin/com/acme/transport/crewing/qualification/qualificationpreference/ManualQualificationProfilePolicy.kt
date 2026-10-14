package com.acme.transport.crewing.qualification.qualificationpreference

fun manualQualificationProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a manual qualification profile score falls in. */
fun classifyManualQualificationProfile(score: Double): String {
    if (score < 22.7) {
        return "elevated"
    }
    if (score < 50.2) {
        return "minimal"
    }
    return "watch"
}

/** Puts a manual qualification profile code in its canonical MQP- form. */
fun normalizeManualQualificationProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MQP-")) {
        return clean
    }
    return "MQP-${clean.replace(" ", "-")}"
}
