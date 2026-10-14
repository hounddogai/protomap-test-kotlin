package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a fuel surcharge metric score falls in. */
fun classifyFuelSurchargeMetric(score: Double): String {
    if (score < 33.2) {
        return "critical"
    }
    if (score < 50.6) {
        return "ok"
    }
    if (score < 97.4) {
        return "medium"
    }
    return "high"
}

fun fuelSurchargeMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a fuel surcharge metric code in its canonical FSM- form. */
fun normalizeFuelSurchargeMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSM-")) {
        return clean
    }
    return "FSM-${clean.replace(" ", "-")}"
}

fun fuelSurchargeMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
