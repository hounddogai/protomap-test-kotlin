package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lenientParcelMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.22
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a lenient parcel mapping code in its canonical LPM- form. */
fun normalizeLenientParcelMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPM-")) {
        return clean
    }
    return "LPM-${clean.replace(" ", "-")}"
}
