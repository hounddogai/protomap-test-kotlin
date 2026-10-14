package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.numbers.roundTo

/** Names the band a cutoff exception score falls in. */
fun classifyCutoffException(score: Double): String {
    if (score < 22.3) {
        return "watch"
    }
    if (score < 81.7) {
        return "minimal"
    }
    if (score < 89.0) {
        return "critical"
    }
    return "ok"
}

fun cutoffExceptionBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 98140.0, in order. */
fun cutoffExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98140.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a cutoff exception code in its canonical CE- form. */
fun normalizeCutoffExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CE-")) {
        return clean
    }
    return "CE-${clean.replace(" ", "-")}"
}
