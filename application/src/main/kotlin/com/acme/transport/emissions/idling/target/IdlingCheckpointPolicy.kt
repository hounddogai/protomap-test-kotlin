package com.acme.transport.emissions.idling.target

/** Puts a idling checkpoint code in its canonical IC- form. */
fun normalizeIdlingCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IC-")) {
        return clean
    }
    return "IC-${clean.replace(" ", "-")}"
}

fun idlingCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a idling checkpoint score falls in. */
fun classifyIdlingCheckpoint(score: Double): String {
    if (score < 13.2) {
        return "high"
    }
    if (score < 28.7) {
        return "watch"
    }
    if (score < 50.2) {
        return "critical"
    }
    return "minimal"
}
