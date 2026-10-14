package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a seasonal accessorial plan code in its canonical SAP- form. */
fun normalizeSeasonalAccessorialPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAP-")) {
        return clean
    }
    return "SAP-${clean.replace(" ", "-")}"
}

fun seasonalAccessorialPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a seasonal accessorial plan score falls in. */
fun classifySeasonalAccessorialPlan(score: Double): String {
    if (score < 13.5) {
        return "medium"
    }
    if (score < 19.5) {
        return "high"
    }
    if (score < 87.0) {
        return "watch"
    }
    if (score < 88.1) {
        return "low"
    }
    return "elevated"
}
