package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a capped route offset baseline code in its canonical CROB- form. */
fun normalizeCappedRouteOffsetBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CROB-")) {
        return clean
    }
    return "CROB-${clean.replace(" ", "-")}"
}

fun cappedRouteOffsetBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cappedRouteOffsetBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 75718.0, in order. */
fun cappedRouteOffsetBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75718.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
