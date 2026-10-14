package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a tiered zone manifest code in its canonical TZM- form. */
fun normalizeTieredZoneManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TZM-")) {
        return clean
    }
    return "TZM-${clean.replace(" ", "-")}"
}

fun tieredZoneManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.85
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
