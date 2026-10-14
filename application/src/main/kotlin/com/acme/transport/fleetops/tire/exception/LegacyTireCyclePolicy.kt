package com.acme.transport.fleetops.tire.exception

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun legacyTireCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.53
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a legacy tire cycle code in its canonical LTC- form. */
fun normalizeLegacyTireCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTC-")) {
        return clean
    }
    return "LTC-${clean.replace(" ", "-")}"
}
