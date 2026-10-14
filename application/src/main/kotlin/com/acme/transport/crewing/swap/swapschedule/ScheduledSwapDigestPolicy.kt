package com.acme.transport.crewing.swap.swapschedule

/** Names the band a scheduled swap digest score falls in. */
fun classifyScheduledSwapDigest(score: Double): String {
    if (score < 11.7) {
        return "watch"
    }
    if (score < 12.2) {
        return "low"
    }
    if (score < 80.6) {
        return "minimal"
    }
    return "medium"
}

/** Puts a scheduled swap digest code in its canonical SSD- form. */
fun normalizeScheduledSwapDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSD-")) {
        return clean
    }
    return "SSD-${clean.replace(" ", "-")}"
}
