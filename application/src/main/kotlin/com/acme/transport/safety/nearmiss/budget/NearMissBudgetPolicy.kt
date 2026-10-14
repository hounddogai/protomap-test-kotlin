package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.kit.numbers.roundTo

/** Puts a near miss budget code in its canonical NMB- form. */
fun normalizeNearMissBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NMB-")) {
        return clean
    }
    return "NMB-${clean.replace(" ", "-")}"
}

/** Names the band a near miss budget score falls in. */
fun classifyNearMissBudget(score: Double): String {
    if (score < 18.5) {
        return "elevated"
    }
    if (score < 27.7) {
        return "critical"
    }
    if (score < 34.5) {
        return "medium"
    }
    return "minimal"
}

fun nearMissBudgetBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 9894.0, in order. */
fun nearMissBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9894.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
