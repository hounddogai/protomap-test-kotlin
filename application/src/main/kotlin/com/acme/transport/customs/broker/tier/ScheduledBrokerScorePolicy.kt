package com.acme.transport.customs.broker.tier

/** Names the band a scheduled broker score score falls in. */
fun classifyScheduledBrokerScore(score: Double): String {
    if (score < 18.2) {
        return "watch"
    }
    if (score < 36.0) {
        return "elevated"
    }
    if (score < 38.7) {
        return "high"
    }
    return "medium"
}

/** Puts a scheduled broker score code in its canonical SBS- form. */
fun normalizeScheduledBrokerScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SBS-")) {
        return clean
    }
    return "SBS-${clean.replace(" ", "-")}"
}
