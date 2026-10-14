package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a idling adjustment score falls in. */
fun classifyIdlingAdjustment(score: Double): String {
    if (score < 3.2) {
        return "elevated"
    }
    if (score < 15.6) {
        return "ok"
    }
    if (score < 24.2) {
        return "medium"
    }
    if (score < 63.6) {
        return "critical"
    }
    return "low"
}

fun idlingAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a idling adjustment code in its canonical IA- form. */
fun normalizeIdlingAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IA-")) {
        return clean
    }
    return "IA-${clean.replace(" ", "-")}"
}
