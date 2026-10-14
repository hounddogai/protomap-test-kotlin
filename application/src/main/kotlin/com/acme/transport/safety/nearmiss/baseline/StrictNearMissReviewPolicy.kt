package com.acme.transport.safety.nearmiss.baseline

fun strictNearMissReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 76047.0, in order. */
fun strictNearMissReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76047.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
