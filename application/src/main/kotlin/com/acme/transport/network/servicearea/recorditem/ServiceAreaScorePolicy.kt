package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a service area score code in its canonical SAS- form. */
fun normalizeServiceAreaScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAS-")) {
        return clean
    }
    return "SAS-${clean.replace(" ", "-")}"
}

fun serviceAreaScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.83
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
