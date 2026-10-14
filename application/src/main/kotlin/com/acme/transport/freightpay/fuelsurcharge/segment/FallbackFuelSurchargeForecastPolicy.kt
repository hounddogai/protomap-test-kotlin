package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a fallback fuel surcharge forecast score falls in. */
fun classifyFallbackFuelSurchargeForecast(score: Double): String {
    if (score < 32.3) {
        return "medium"
    }
    if (score < 85.0) {
        return "minimal"
    }
    return "elevated"
}

fun fallbackFuelSurchargeForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun fallbackFuelSurchargeForecastBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
