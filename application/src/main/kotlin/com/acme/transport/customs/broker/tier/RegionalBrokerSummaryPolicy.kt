package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 75471.0, in order. */
fun regionalBrokerSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75471.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a regional broker summary code in its canonical RBS- form. */
fun normalizeRegionalBrokerSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBS-")) {
        return clean
    }
    return "RBS-${clean.replace(" ", "-")}"
}

fun regionalBrokerSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
