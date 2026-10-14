package com.acme.transport.customs.clearance.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackClearanceQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.0
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a fallback clearance quota code in its canonical FCQ- form. */
fun normalizeFallbackClearanceQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FCQ-")) {
        return clean
    }
    return "FCQ-${clean.replace(" ", "-")}"
}
