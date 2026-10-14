package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun defaultRedeliveryBundleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun defaultRedeliveryBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a default redelivery bundle code in its canonical DRB- form. */
fun normalizeDefaultRedeliveryBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DRB-")) {
        return clean
    }
    return "DRB-${clean.replace(" ", "-")}"
}
