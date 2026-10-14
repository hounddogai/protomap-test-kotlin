package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a seasonal attempt estimate code in its canonical SAE- form. */
fun normalizeSeasonalAttemptEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAE-")) {
        return clean
    }
    return "SAE-${clean.replace(" ", "-")}"
}

fun seasonalAttemptEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
