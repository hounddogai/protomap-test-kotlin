package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a trailer manifest code in its canonical TM- form. */
fun normalizeTrailerManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TM-")) {
        return clean
    }
    return "TM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 56237.0, in order. */
fun trailerManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56237.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun trailerManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun trailerManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
