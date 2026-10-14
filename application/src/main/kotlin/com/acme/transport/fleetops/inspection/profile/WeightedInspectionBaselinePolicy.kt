package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a weighted inspection baseline code in its canonical WIB- form. */
fun normalizeWeightedInspectionBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WIB-")) {
        return clean
    }
    return "WIB-${clean.replace(" ", "-")}"
}

fun weightedInspectionBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
