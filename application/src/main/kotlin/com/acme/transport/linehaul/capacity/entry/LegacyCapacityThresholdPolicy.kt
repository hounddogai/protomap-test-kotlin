package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a legacy capacity threshold code in its canonical LCT- form. */
fun normalizeLegacyCapacityThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCT-")) {
        return clean
    }
    return "LCT-${clean.replace(" ", "-")}"
}

fun legacyCapacityThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
