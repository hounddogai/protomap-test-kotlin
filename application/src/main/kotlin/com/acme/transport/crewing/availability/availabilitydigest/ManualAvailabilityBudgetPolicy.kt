package com.acme.transport.crewing.availability.availabilitydigest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualAvailabilityBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a manual availability budget code in its canonical MAB- form. */
fun normalizeManualAvailabilityBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MAB-")) {
        return clean
    }
    return "MAB-${clean.replace(" ", "-")}"
}
