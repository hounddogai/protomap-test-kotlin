package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveIdlingPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.19
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a adaptive idling policy code in its canonical AIP- form. */
fun normalizeAdaptiveIdlingPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AIP-")) {
        return clean
    }
    return "AIP-${clean.replace(" ", "-")}"
}
