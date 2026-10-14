package com.acme.transport.network.servicearea.target

/** Puts a service area target code in its canonical SAT- form. */
fun normalizeServiceAreaTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAT-")) {
        return clean
    }
    return "SAT-${clean.replace(" ", "-")}"
}

/** Names the band a service area target score falls in. */
fun classifyServiceAreaTarget(score: Double): String {
    if (score < 3.8) {
        return "critical"
    }
    if (score < 16.1) {
        return "ok"
    }
    if (score < 23.3) {
        return "low"
    }
    if (score < 25.6) {
        return "watch"
    }
    return "medium"
}
