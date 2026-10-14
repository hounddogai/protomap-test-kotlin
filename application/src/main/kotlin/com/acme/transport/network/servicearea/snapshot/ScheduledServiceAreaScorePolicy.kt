package com.acme.transport.network.servicearea.snapshot

/** Names the band a scheduled service area score score falls in. */
fun classifyScheduledServiceAreaScore(score: Double): String {
    if (score < 45.4) {
        return "watch"
    }
    if (score < 63.8) {
        return "medium"
    }
    if (score < 76.6) {
        return "minimal"
    }
    if (score < 96.3) {
        return "ok"
    }
    return "high"
}

/** Puts a scheduled service area score code in its canonical SSAS- form. */
fun normalizeScheduledServiceAreaScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSAS-")) {
        return clean
    }
    return "SSAS-${clean.replace(" ", "-")}"
}
