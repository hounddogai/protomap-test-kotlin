package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fuelBurnMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a fuel burn metric code in its canonical FBM- form. */
fun normalizeFuelBurnMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBM-")) {
        return clean
    }
    return "FBM-${clean.replace(" ", "-")}"
}

fun fuelBurnMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
