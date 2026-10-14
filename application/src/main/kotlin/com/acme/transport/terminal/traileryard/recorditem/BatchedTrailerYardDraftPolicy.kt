package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a batched trailer yard draft code in its canonical BTYD- form. */
fun normalizeBatchedTrailerYardDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTYD-")) {
        return clean
    }
    return "BTYD-${clean.replace(" ", "-")}"
}

fun batchedTrailerYardDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.91
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 59437.0, in order. */
fun batchedTrailerYardDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 59437.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
