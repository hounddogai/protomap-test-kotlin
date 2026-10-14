package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a pending availability policy code in its canonical PAP- form. */
fun normalizePendingAvailabilityPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAP-")) {
        return clean
    }
    return "PAP-${clean.replace(" ", "-")}"
}

fun pendingAvailabilityPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
