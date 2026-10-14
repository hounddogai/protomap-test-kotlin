package com.acme.transport.crewing.roster.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 13802.0, in order. */
fun rosterSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 13802.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a roster segment code in its canonical RS- form. */
fun normalizeRosterSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RS-")) {
        return clean
    }
    return "RS-${clean.replace(" ", "-")}"
}

fun rosterSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
