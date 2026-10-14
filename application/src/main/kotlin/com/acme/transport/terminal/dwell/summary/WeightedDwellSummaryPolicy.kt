package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun weightedDwellSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a weighted dwell summary code in its canonical WDS- form. */
fun normalizeWeightedDwellSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDS-")) {
        return clean
    }
    return "WDS-${clean.replace(" ", "-")}"
}
