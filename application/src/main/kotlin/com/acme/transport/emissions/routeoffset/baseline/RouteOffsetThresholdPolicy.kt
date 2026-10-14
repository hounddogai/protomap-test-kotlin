package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a route offset threshold code in its canonical ROT- form. */
fun normalizeRouteOffsetThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROT-")) {
        return clean
    }
    return "ROT-${clean.replace(" ", "-")}"
}

fun routeOffsetThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
