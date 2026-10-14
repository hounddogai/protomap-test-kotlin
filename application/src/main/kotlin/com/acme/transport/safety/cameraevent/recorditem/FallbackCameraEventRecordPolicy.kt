package com.acme.transport.safety.cameraevent.recorditem

fun fallbackCameraEventRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Puts a fallback camera event record code in its canonical FCER- form. */
fun normalizeFallbackCameraEventRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FCER-")) {
        return clean
    }
    return "FCER-${clean.replace(" ", "-")}"
}

/** Names the band a fallback camera event record score falls in. */
fun classifyFallbackCameraEventRecord(score: Double): String {
    if (score < 16.2) {
        return "high"
    }
    if (score < 41.8) {
        return "ok"
    }
    if (score < 88.6) {
        return "watch"
    }
    if (score < 92.4) {
        return "medium"
    }
    return "critical"
}
