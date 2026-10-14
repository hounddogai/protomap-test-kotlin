package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a sortation audit code in its canonical SA- form. */
fun normalizeSortationAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SA-")) {
        return clean
    }
    return "SA-${clean.replace(" ", "-")}"
}

fun sortationAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
