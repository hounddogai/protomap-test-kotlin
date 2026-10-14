package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalFuelSurchargeThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun globalFuelSurchargeThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a global fuel surcharge threshold code in its canonical GFST- form. */
fun normalizeGlobalFuelSurchargeThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GFST-")) {
        return clean
    }
    return "GFST-${clean.replace(" ", "-")}"
}
