package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalFuelSurchargeOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Puts a global fuel surcharge override code in its canonical GFSO- form. */
fun normalizeGlobalFuelSurchargeOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GFSO-")) {
        return clean
    }
    return "GFSO-${clean.replace(" ", "-")}"
}

fun globalFuelSurchargeOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
