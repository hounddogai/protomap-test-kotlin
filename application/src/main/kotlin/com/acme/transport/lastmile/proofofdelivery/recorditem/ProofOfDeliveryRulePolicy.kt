package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun proofOfDeliveryRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun proofOfDeliveryRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a proof of delivery rule code in its canonical PODR- form. */
fun normalizeProofOfDeliveryRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODR-")) {
        return clean
    }
    return "PODR-${clean.replace(" ", "-")}"
}
