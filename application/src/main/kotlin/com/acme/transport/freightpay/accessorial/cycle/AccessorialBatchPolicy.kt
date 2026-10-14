package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 70636.0, in order. */
fun accessorialBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70636.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun accessorialBatchBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun accessorialBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a accessorial batch score falls in. */
fun classifyAccessorialBatch(score: Double): String {
    if (score < 34.6) {
        return "critical"
    }
    if (score < 53.1) {
        return "elevated"
    }
    if (score < 54.1) {
        return "medium"
    }
    if (score < 98.2) {
        return "ok"
    }
    return "minimal"
}
