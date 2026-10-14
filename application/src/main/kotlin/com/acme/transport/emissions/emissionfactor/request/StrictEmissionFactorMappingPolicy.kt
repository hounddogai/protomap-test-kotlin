package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun strictEmissionFactorMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a strict emission factor mapping code in its canonical SEFM- form. */
fun normalizeStrictEmissionFactorMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SEFM-")) {
        return clean
    }
    return "SEFM-${clean.replace(" ", "-")}"
}
