package com.acme.transport.terminal.sortation.forecast

fun sortationForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a sortation forecast code in its canonical SF- form. */
fun normalizeSortationForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SF-")) {
        return clean
    }
    return "SF-${clean.replace(" ", "-")}"
}

/** Names the band a sortation forecast score falls in. */
fun classifySortationForecast(score: Double): String {
    if (score < 48.1) {
        return "elevated"
    }
    if (score < 85.5) {
        return "high"
    }
    if (score < 88.2) {
        return "minimal"
    }
    return "watch"
}
