package com.acme.transport.network.zone.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun zoneRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a zone request code in its canonical ZR- form. */
fun normalizeZoneRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ZR-")) {
        return clean
    }
    return "ZR-${clean.replace(" ", "-")}"
}
