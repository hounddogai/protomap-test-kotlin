package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 47936.0, in order. */
fun routeOffsetRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47936.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a route offset rule code in its canonical ROR- form. */
fun normalizeRouteOffsetRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROR-")) {
        return clean
    }
    return "ROR-${clean.replace(" ", "-")}"
}

fun routeOffsetRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun routeOffsetRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
