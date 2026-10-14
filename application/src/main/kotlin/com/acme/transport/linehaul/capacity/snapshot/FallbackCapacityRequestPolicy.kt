package com.acme.transport.linehaul.capacity.snapshot

/** Names the band a fallback capacity request score falls in. */
fun classifyFallbackCapacityRequest(score: Double): String {
    if (score < 22.3) {
        return "ok"
    }
    if (score < 45.9) {
        return "low"
    }
    if (score < 55.2) {
        return "watch"
    }
    if (score < 68.6) {
        return "minimal"
    }
    return "medium"
}

/** Puts a fallback capacity request code in its canonical FCR- form. */
fun normalizeFallbackCapacityRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FCR-")) {
        return clean
    }
    return "FCR-${clean.replace(" ", "-")}"
}
