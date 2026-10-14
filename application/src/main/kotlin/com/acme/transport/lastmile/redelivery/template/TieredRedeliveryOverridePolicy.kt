package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a tiered redelivery override code in its canonical TRO- form. */
fun normalizeTieredRedeliveryOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TRO-")) {
        return clean
    }
    return "TRO-${clean.replace(" ", "-")}"
}

fun tieredRedeliveryOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
