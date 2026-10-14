package com.acme.transport.lastmile.drop.milestone

/** Puts a drop segment code in its canonical DS- form. */
fun normalizeDropSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DS-")) {
        return clean
    }
    return "DS-${clean.replace(" ", "-")}"
}

/** Names the band a drop segment score falls in. */
fun classifyDropSegment(score: Double): String {
    if (score < 4.7) {
        return "critical"
    }
    if (score < 83.1) {
        return "minimal"
    }
    if (score < 93.3) {
        return "elevated"
    }
    if (score < 93.3) {
        return "medium"
    }
    return "watch"
}
