package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 51665.0, in order. */
fun emissionFactorReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51665.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeEmissionFactorReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a emission factor reservation code in its canonical EFR- form. */
fun normalizeEmissionFactorReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFR-")) {
        return clean
    }
    return "EFR-${clean.replace(" ", "-")}"
}
