package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveEmissionFactorGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun adaptiveEmissionFactorGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 17352.0, in order. */
fun adaptiveEmissionFactorGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17352.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a adaptive emission factor group code in its canonical AEFG- form. */
fun normalizeAdaptiveEmissionFactorGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AEFG-")) {
        return clean
    }
    return "AEFG-${clean.replace(" ", "-")}"
}
