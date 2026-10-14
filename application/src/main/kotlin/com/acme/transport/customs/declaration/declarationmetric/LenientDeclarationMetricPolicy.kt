package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lenientDeclarationMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a lenient declaration metric code in its canonical LDM- form. */
fun normalizeLenientDeclarationMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDM-")) {
        return clean
    }
    return "LDM-${clean.replace(" ", "-")}"
}

fun lenientDeclarationMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
