package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a strict emission factor cycle code in its canonical SEFC- form. */
fun normalizeStrictEmissionFactorCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SEFC-")) {
        return clean
    }
    return "SEFC-${clean.replace(" ", "-")}"
}

fun strictEmissionFactorCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun strictEmissionFactorCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 57130.0, in order. */
fun strictEmissionFactorCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57130.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
