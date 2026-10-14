package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a emission factor profile code in its canonical EFP- form. */
fun normalizeEmissionFactorProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFP-")) {
        return clean
    }
    return "EFP-${clean.replace(" ", "-")}"
}

fun emissionFactorProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
