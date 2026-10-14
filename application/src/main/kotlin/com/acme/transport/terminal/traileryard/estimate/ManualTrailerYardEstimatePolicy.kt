package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualTrailerYardEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a manual trailer yard estimate code in its canonical MTYE- form. */
fun normalizeManualTrailerYardEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTYE-")) {
        return clean
    }
    return "MTYE-${clean.replace(" ", "-")}"
}
