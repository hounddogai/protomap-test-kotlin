package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lenientServiceAreaRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.83
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a lenient service area request code in its canonical LSAR- form. */
fun normalizeLenientServiceAreaRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSAR-")) {
        return clean
    }
    return "LSAR-${clean.replace(" ", "-")}"
}
