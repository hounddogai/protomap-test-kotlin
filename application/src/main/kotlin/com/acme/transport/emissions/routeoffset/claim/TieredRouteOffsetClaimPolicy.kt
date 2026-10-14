package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 80784.0, in order. */
fun tieredRouteOffsetClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80784.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered route offset claim code in its canonical TROC- form. */
fun normalizeTieredRouteOffsetClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TROC-")) {
        return clean
    }
    return "TROC-${clean.replace(" ", "-")}"
}

fun tieredRouteOffsetClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
