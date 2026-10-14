package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 25933.0, in order. */
fun batchedRestBreakThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25933.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedRestBreakThresholdBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a batched rest break threshold code in its canonical BRBT- form. */
fun normalizeBatchedRestBreakThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BRBT-")) {
        return clean
    }
    return "BRBT-${clean.replace(" ", "-")}"
}
