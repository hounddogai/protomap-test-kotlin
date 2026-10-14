package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 53830.0, in order. */
fun cappedScorecardCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53830.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedScorecardCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun cappedScorecardCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a capped scorecard checkpoint code in its canonical CSC- form. */
fun normalizeCappedScorecardCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CSC-")) {
        return clean
    }
    return "CSC-${clean.replace(" ", "-")}"
}
