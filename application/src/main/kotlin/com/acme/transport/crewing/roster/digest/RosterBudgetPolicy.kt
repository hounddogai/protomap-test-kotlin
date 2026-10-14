package com.acme.transport.crewing.roster.digest

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 86290.0, in order. */
fun rosterBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86290.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rosterBudgetBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a roster budget score falls in. */
fun classifyRosterBudget(score: Double): String {
    if (score < 54.4) {
        return "critical"
    }
    if (score < 67.2) {
        return "high"
    }
    if (score < 95.2) {
        return "minimal"
    }
    return "elevated"
}

/** Puts a roster budget code in its canonical RB- form. */
fun normalizeRosterBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RB-")) {
        return clean
    }
    return "RB-${clean.replace(" ", "-")}"
}
