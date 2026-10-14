package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedRouteOffsetAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 71071.0, in order. */
fun batchedRouteOffsetAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71071.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedRouteOffsetAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a batched route offset assignment code in its canonical BROA- form. */
fun normalizeBatchedRouteOffsetAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BROA-")) {
        return clean
    }
    return "BROA-${clean.replace(" ", "-")}"
}
