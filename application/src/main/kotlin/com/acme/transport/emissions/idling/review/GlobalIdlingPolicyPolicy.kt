package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalIdlingPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.06
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a global idling policy code in its canonical GIP- form. */
fun normalizeGlobalIdlingPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GIP-")) {
        return clean
    }
    return "GIP-${clean.replace(" ", "-")}"
}
