package com.acme.transport.crewing.swap.bundle

/** Counts how many amounts fit in a budget of 79473.0, in order. */
fun scheduledSwapSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79473.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a scheduled swap slot score falls in. */
fun classifyScheduledSwapSlot(score: Double): String {
    if (score < 5.4) {
        return "low"
    }
    if (score < 75.7) {
        return "minimal"
    }
    return "elevated"
}

fun scheduledSwapSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
