package com.acme.transport.fleetops.tire.quota

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a seasonal tire quota code in its canonical STQ- form. */
fun normalizeSeasonalTireQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STQ-")) {
        return clean
    }
    return "STQ-${clean.replace(" ", "-")}"
}

fun seasonalTireQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
