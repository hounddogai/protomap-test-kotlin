package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultParcelGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default parcel group score falls in. */
fun classifyDefaultParcelGroup(score: Double): String {
    if (score < 76.4) {
        return "elevated"
    }
    if (score < 87.4) {
        return "low"
    }
    return "medium"
}

/** Puts a default parcel group code in its canonical DPG- form. */
fun normalizeDefaultParcelGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPG-")) {
        return clean
    }
    return "DPG-${clean.replace(" ", "-")}"
}
