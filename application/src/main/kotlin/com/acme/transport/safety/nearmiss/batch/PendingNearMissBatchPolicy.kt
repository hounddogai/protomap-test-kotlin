package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.numbers.roundTo

/** Puts a pending near miss batch code in its canonical PNMB- form. */
fun normalizePendingNearMissBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PNMB-")) {
        return clean
    }
    return "PNMB-${clean.replace(" ", "-")}"
}

/** Names the band a pending near miss batch score falls in. */
fun classifyPendingNearMissBatch(score: Double): String {
    if (score < 22.0) {
        return "medium"
    }
    if (score < 28.1) {
        return "ok"
    }
    if (score < 43.5) {
        return "critical"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 4217.0, in order. */
fun pendingNearMissBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4217.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingNearMissBatchBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
