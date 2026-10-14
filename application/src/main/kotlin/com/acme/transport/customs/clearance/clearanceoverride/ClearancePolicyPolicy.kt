package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a clearance policy code in its canonical CP- form. */
fun normalizeClearancePolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CP-")) {
        return clean
    }
    return "CP-${clean.replace(" ", "-")}"
}

fun clearancePolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
