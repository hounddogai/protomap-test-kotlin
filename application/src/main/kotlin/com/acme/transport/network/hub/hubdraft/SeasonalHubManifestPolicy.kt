package com.acme.transport.network.hub.hubdraft

/** Puts a seasonal hub manifest code in its canonical SHM- form. */
fun normalizeSeasonalHubManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SHM-")) {
        return clean
    }
    return "SHM-${clean.replace(" ", "-")}"
}

/** Names the band a seasonal hub manifest score falls in. */
fun classifySeasonalHubManifest(score: Double): String {
    if (score < 9.5) {
        return "minimal"
    }
    if (score < 57.4) {
        return "watch"
    }
    if (score < 68.0) {
        return "critical"
    }
    if (score < 82.5) {
        return "low"
    }
    return "ok"
}

fun seasonalHubManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
