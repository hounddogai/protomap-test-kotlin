package com.acme.transport.emissions.idling.snapshot

/** Names the band a adaptive idling segment score falls in. */
fun classifyAdaptiveIdlingSegment(score: Double): String {
    if (score < 17.2) {
        return "medium"
    }
    if (score < 35.7) {
        return "minimal"
    }
    return "ok"
}

/** Puts a adaptive idling segment code in its canonical AIS- form. */
fun normalizeAdaptiveIdlingSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AIS-")) {
        return clean
    }
    return "AIS-${clean.replace(" ", "-")}"
}
