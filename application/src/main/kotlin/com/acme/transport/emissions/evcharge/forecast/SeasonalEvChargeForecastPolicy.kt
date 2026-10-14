package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a seasonal ev charge forecast code in its canonical SECF- form. */
fun normalizeSeasonalEvChargeForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SECF-")) {
        return clean
    }
    return "SECF-${clean.replace(" ", "-")}"
}

fun seasonalEvChargeForecastBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a seasonal ev charge forecast score falls in. */
fun classifySeasonalEvChargeForecast(score: Double): String {
    if (score < 41.4) {
        return "high"
    }
    if (score < 45.4) {
        return "medium"
    }
    if (score < 63.2) {
        return "low"
    }
    if (score < 74.4) {
        return "critical"
    }
    return "ok"
}

fun seasonalEvChargeForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
