package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a pending idling profile code in its canonical PIP- form. */
fun normalizePendingIdlingProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PIP-")) {
        return clean
    }
    return "PIP-${clean.replace(" ", "-")}"
}

fun pendingIdlingProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
