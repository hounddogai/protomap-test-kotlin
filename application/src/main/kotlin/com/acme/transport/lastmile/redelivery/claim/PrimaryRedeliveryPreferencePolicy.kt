package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a primary redelivery preference code in its canonical PRP- form. */
fun normalizePrimaryRedeliveryPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRP-")) {
        return clean
    }
    return "PRP-${clean.replace(" ", "-")}"
}

fun primaryRedeliveryPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun primaryRedeliveryPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
