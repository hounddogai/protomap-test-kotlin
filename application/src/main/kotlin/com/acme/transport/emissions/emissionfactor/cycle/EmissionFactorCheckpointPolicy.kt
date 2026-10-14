package com.acme.transport.emissions.emissionfactor.cycle

/** Names the band a emission factor checkpoint score falls in. */
fun classifyEmissionFactorCheckpoint(score: Double): String {
    if (score < 53.3) {
        return "critical"
    }
    if (score < 66.6) {
        return "ok"
    }
    if (score < 95.5) {
        return "low"
    }
    return "watch"
}

/** Puts a emission factor checkpoint code in its canonical EFC- form. */
fun normalizeEmissionFactorCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFC-")) {
        return clean
    }
    return "EFC-${clean.replace(" ", "-")}"
}

fun emissionFactorCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
