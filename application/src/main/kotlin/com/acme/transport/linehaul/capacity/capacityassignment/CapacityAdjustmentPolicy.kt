package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun capacityAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.45
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun capacityAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a capacity adjustment score falls in. */
fun classifyCapacityAdjustment(score: Double): String {
    if (score < 12.7) {
        return "critical"
    }
    if (score < 32.8) {
        return "high"
    }
    if (score < 39.9) {
        return "low"
    }
    if (score < 92.7) {
        return "ok"
    }
    return "watch"
}

/** Puts a capacity adjustment code in its canonical CA- form. */
fun normalizeCapacityAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CA-")) {
        return clean
    }
    return "CA-${clean.replace(" ", "-")}"
}
