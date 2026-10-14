package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 82701.0, in order. */
fun weightedLockerExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82701.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun weightedLockerExceptionBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a weighted locker exception code in its canonical WLE- form. */
fun normalizeWeightedLockerExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WLE-")) {
        return clean
    }
    return "WLE-${clean.replace(" ", "-")}"
}

/** Names the band a weighted locker exception score falls in. */
fun classifyWeightedLockerException(score: Double): String {
    if (score < 17.6) {
        return "watch"
    }
    if (score < 74.7) {
        return "medium"
    }
    if (score < 75.8) {
        return "minimal"
    }
    if (score < 78.1) {
        return "elevated"
    }
    return "ok"
}
