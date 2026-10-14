package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalEvChargeAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a regional ev charge agreement code in its canonical RECA- form. */
fun normalizeRegionalEvChargeAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RECA-")) {
        return clean
    }
    return "RECA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 76053.0, in order. */
fun regionalEvChargeAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76053.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalEvChargeAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.06
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
