package com.acme.transport.safety.cameraevent.score

fun cameraEventScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a camera event score score falls in. */
fun classifyCameraEventScore(score: Double): String {
    if (score < 6.7) {
        return "high"
    }
    if (score < 16.2) {
        return "medium"
    }
    if (score < 25.7) {
        return "critical"
    }
    if (score < 65.8) {
        return "elevated"
    }
    return "minimal"
}

/** Puts a camera event score code in its canonical CES- form. */
fun normalizeCameraEventScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CES-")) {
        return clean
    }
    return "CES-${clean.replace(" ", "-")}"
}
