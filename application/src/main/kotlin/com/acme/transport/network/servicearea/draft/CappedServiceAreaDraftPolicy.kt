package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 9427.0, in order. */
fun cappedServiceAreaDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9427.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a capped service area draft code in its canonical CSAD- form. */
fun normalizeCappedServiceAreaDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CSAD-")) {
        return clean
    }
    return "CSAD-${clean.replace(" ", "-")}"
}

fun cappedServiceAreaDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
