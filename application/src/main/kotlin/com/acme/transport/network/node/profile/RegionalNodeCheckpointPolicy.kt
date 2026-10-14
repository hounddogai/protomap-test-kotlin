package com.acme.transport.network.node.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalNodeCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 40402.0, in order. */
fun regionalNodeCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40402.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a regional node checkpoint code in its canonical RNC- form. */
fun normalizeRegionalNodeCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RNC-")) {
        return clean
    }
    return "RNC-${clean.replace(" ", "-")}"
}

fun regionalNodeCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
