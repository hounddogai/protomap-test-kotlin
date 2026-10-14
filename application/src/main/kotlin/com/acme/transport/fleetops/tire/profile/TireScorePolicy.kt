package com.acme.transport.fleetops.tire.profile

/** Names the band a tire score score falls in. */
fun classifyTireScore(score: Double): String {
    if (score < 32.2) {
        return "ok"
    }
    if (score < 50.6) {
        return "low"
    }
    if (score < 63.2) {
        return "elevated"
    }
    if (score < 98.7) {
        return "high"
    }
    return "critical"
}

fun tireScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a tire score code in its canonical TS- form. */
fun normalizeTireScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TS-")) {
        return clean
    }
    return "TS-${clean.replace(" ", "-")}"
}
