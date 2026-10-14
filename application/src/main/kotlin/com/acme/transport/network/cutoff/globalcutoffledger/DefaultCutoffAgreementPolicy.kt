package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultCutoffAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 48304.0, in order. */
fun defaultCutoffAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48304.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a default cutoff agreement code in its canonical DCA- form. */
fun normalizeDefaultCutoffAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DCA-")) {
        return clean
    }
    return "DCA-${clean.replace(" ", "-")}"
}
