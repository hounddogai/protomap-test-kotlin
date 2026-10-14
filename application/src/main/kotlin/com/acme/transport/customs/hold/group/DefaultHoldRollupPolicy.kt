package com.acme.transport.customs.hold.group

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultHoldRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a default hold rollup code in its canonical DHR- form. */
fun normalizeDefaultHoldRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DHR-")) {
        return clean
    }
    return "DHR-${clean.replace(" ", "-")}"
}
