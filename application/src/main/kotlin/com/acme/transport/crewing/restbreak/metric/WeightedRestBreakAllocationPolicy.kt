package com.acme.transport.crewing.restbreak.metric

/** Counts how many amounts fit in a budget of 9947.0, in order. */
fun weightedRestBreakAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9947.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted rest break allocation score falls in. */
fun classifyWeightedRestBreakAllocation(score: Double): String {
    if (score < 5.6) {
        return "elevated"
    }
    if (score < 51.6) {
        return "high"
    }
    if (score < 67.6) {
        return "medium"
    }
    if (score < 89.1) {
        return "watch"
    }
    return "minimal"
}

fun weightedRestBreakAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
