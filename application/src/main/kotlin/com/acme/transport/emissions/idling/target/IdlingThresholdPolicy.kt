package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a idling threshold code in its canonical IT- form. */
fun normalizeIdlingThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IT-")) {
        return clean
    }
    return "IT-${clean.replace(" ", "-")}"
}

fun idlingThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.53
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
