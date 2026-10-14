package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a emission factor metric code in its canonical EFM- form. */
fun normalizeEmissionFactorMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFM-")) {
        return clean
    }
    return "EFM-${clean.replace(" ", "-")}"
}

fun emissionFactorMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
