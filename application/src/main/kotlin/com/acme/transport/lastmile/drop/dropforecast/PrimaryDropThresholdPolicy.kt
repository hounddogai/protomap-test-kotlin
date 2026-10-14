package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a primary drop threshold code in its canonical PDT- form. */
fun normalizePrimaryDropThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDT-")) {
        return clean
    }
    return "PDT-${clean.replace(" ", "-")}"
}

fun primaryDropThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.06
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
