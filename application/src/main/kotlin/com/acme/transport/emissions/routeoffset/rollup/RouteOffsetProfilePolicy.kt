package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 45071.0, in order. */
fun routeOffsetProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45071.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a route offset profile code in its canonical ROP- form. */
fun normalizeRouteOffsetProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROP-")) {
        return clean
    }
    return "ROP-${clean.replace(" ", "-")}"
}

fun routeOffsetProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
