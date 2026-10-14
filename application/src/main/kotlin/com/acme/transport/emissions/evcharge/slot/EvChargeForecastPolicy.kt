package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.numbers.roundTo

fun evChargeForecastBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a ev charge forecast code in its canonical ECF- form. */
fun normalizeEvChargeForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ECF-")) {
        return clean
    }
    return "ECF-${clean.replace(" ", "-")}"
}

/** Names the band a ev charge forecast score falls in. */
fun classifyEvChargeForecast(score: Double): String {
    if (score < 4.8) {
        return "critical"
    }
    if (score < 65.0) {
        return "low"
    }
    if (score < 90.4) {
        return "watch"
    }
    return "minimal"
}
