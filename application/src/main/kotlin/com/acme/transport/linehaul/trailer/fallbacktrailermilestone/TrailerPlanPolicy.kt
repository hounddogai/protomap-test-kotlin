package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.numbers.roundTo

fun trailerPlanBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a trailer plan code in its canonical TP- form. */
fun normalizeTrailerPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TP-")) {
        return clean
    }
    return "TP-${clean.replace(" ", "-")}"
}

/** Names the band a trailer plan score falls in. */
fun classifyTrailerPlan(score: Double): String {
    if (score < 50.7) {
        return "medium"
    }
    if (score < 82.5) {
        return "low"
    }
    if (score < 96.1) {
        return "high"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 23359.0, in order. */
fun trailerPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23359.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
