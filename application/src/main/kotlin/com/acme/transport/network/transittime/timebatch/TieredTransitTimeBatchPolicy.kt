package com.acme.transport.network.transittime.timebatch

/** Puts a tiered transit time batch code in its canonical TTTB- form. */
fun normalizeTieredTransitTimeBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTTB-")) {
        return clean
    }
    return "TTTB-${clean.replace(" ", "-")}"
}

/** Names the band a tiered transit time batch score falls in. */
fun classifyTieredTransitTimeBatch(score: Double): String {
    if (score < 2.6) {
        return "watch"
    }
    if (score < 45.5) {
        return "low"
    }
    if (score < 64.0) {
        return "high"
    }
    return "elevated"
}
