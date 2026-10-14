package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a sortation estimate code in its canonical SE- form. */
fun normalizeSortationEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SE-")) {
        return clean
    }
    return "SE-${clean.replace(" ", "-")}"
}

fun sortationEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
