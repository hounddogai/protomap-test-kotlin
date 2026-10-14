package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a weighted dispute rule code in its canonical WDR- form. */
fun normalizeWeightedDisputeRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDR-")) {
        return clean
    }
    return "WDR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 21053.0, in order. */
fun weightedDisputeRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21053.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun weightedDisputeRuleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun weightedDisputeRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
