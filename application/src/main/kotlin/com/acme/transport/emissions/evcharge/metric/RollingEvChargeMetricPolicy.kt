package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun rollingEvChargeMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a rolling ev charge metric code in its canonical RECM- form. */
fun normalizeRollingEvChargeMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RECM-")) {
        return clean
    }
    return "RECM-${clean.replace(" ", "-")}"
}

fun rollingEvChargeMetricBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
