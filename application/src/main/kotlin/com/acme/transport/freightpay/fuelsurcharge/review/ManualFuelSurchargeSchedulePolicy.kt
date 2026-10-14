package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun manualFuelSurchargeScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a manual fuel surcharge schedule score falls in. */
fun classifyManualFuelSurchargeSchedule(score: Double): String {
    if (score < 60.1) {
        return "critical"
    }
    if (score < 60.9) {
        return "minimal"
    }
    if (score < 90.3) {
        return "low"
    }
    return "medium"
}

/** Puts a manual fuel surcharge schedule code in its canonical MFSS- form. */
fun normalizeManualFuelSurchargeScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MFSS-")) {
        return clean
    }
    return "MFSS-${clean.replace(" ", "-")}"
}

fun manualFuelSurchargeScheduleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
