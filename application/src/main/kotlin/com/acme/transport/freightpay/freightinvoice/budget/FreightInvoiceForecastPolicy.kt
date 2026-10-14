package com.acme.transport.freightpay.freightinvoice.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun freightInvoiceForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.03
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a freight invoice forecast score falls in. */
fun classifyFreightInvoiceForecast(score: Double): String {
    if (score < 55.0) {
        return "critical"
    }
    if (score < 88.3) {
        return "minimal"
    }
    return "low"
}
