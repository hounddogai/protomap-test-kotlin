package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a attempt override code in its canonical AO- form. */
fun normalizeAttemptOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AO-")) {
        return clean
    }
    return "AO-${clean.replace(" ", "-")}"
}

fun attemptOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
