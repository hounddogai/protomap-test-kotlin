package com.acme.transport.freightpay.accessorial.accessorialplan

/** Names the band a accessorial estimate score falls in. */
fun classifyAccessorialEstimate(score: Double): String {
    if (score < 37.4) {
        return "elevated"
    }
    if (score < 40.0) {
        return "ok"
    }
    if (score < 65.8) {
        return "medium"
    }
    if (score < 92.4) {
        return "watch"
    }
    return "low"
}

fun accessorialEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a accessorial estimate code in its canonical AE- form. */
fun normalizeAccessorialEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AE-")) {
        return clean
    }
    return "AE-${clean.replace(" ", "-")}"
}
