package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.numbers.roundTo

fun holdReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a hold review code in its canonical HR- form. */
fun normalizeHoldReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HR-")) {
        return clean
    }
    return "HR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 70985.0, in order. */
fun holdReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70985.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun holdReviewBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
