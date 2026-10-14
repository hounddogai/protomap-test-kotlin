package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.numbers.roundTo

fun routeOffsetForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun routeOffsetForecastBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a route offset forecast code in its canonical ROF- form. */
fun normalizeRouteOffsetForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROF-")) {
        return clean
    }
    return "ROF-${clean.replace(" ", "-")}"
}

/** Names the band a route offset forecast score falls in. */
fun classifyRouteOffsetForecast(score: Double): String {
    if (score < 17.4) {
        return "ok"
    }
    if (score < 39.0) {
        return "high"
    }
    if (score < 79.7) {
        return "low"
    }
    return "medium"
}
