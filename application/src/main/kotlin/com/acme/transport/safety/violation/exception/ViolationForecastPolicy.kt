package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.numbers.roundTo

/** Puts a violation forecast code in its canonical VF- form. */
fun normalizeViolationForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("VF-")) {
        return clean
    }
    return "VF-${clean.replace(" ", "-")}"
}

fun violationForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a violation forecast score falls in. */
fun classifyViolationForecast(score: Double): String {
    if (score < 12.3) {
        return "critical"
    }
    if (score < 72.7) {
        return "medium"
    }
    if (score < 89.6) {
        return "watch"
    }
    return "low"
}

fun violationForecastBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
