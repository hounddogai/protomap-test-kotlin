package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 43892.0, in order. */
fun attemptNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43892.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun attemptNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a attempt notice score falls in. */
fun classifyAttemptNotice(score: Double): String {
    if (score < 72.0) {
        return "low"
    }
    if (score < 90.1) {
        return "elevated"
    }
    return "medium"
}

fun attemptNoticeBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
