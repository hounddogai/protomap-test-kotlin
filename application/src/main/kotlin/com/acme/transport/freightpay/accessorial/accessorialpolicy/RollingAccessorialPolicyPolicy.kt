package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a rolling accessorial policy code in its canonical RAP- form. */
fun normalizeRollingAccessorialPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAP-")) {
        return clean
    }
    return "RAP-${clean.replace(" ", "-")}"
}

fun rollingAccessorialPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
