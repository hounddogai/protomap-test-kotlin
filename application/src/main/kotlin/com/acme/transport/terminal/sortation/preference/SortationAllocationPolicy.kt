package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a sortation allocation code in its canonical SA- form. */
fun normalizeSortationAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SA-")) {
        return clean
    }
    return "SA-${clean.replace(" ", "-")}"
}

fun sortationAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.49
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
