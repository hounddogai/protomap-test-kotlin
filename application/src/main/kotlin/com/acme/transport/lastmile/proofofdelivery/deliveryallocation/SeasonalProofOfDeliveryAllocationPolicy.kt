package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalProofOfDeliveryAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a seasonal proof of delivery allocation code in its canonical SPODA- form. */
fun normalizeSeasonalProofOfDeliveryAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPODA-")) {
        return clean
    }
    return "SPODA-${clean.replace(" ", "-")}"
}

fun seasonalProofOfDeliveryAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
