package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.numbers.roundTo

fun fallbackDropForecastBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun fallbackDropForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a fallback drop forecast code in its canonical FDF- form. */
fun normalizeFallbackDropForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDF-")) {
        return clean
    }
    return "FDF-${clean.replace(" ", "-")}"
}
