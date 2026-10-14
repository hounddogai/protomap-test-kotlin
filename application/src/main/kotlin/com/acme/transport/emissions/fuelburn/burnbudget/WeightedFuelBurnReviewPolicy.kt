package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun weightedFuelBurnReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 28884.0, in order. */
fun weightedFuelBurnReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28884.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun weightedFuelBurnReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a weighted fuel burn review code in its canonical WFBR- form. */
fun normalizeWeightedFuelBurnReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WFBR-")) {
        return clean
    }
    return "WFBR-${clean.replace(" ", "-")}"
}
