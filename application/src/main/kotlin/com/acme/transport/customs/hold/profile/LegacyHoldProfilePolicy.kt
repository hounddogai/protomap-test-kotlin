package com.acme.transport.customs.hold.profile

/** Counts how many amounts fit in a budget of 91787.0, in order. */
fun legacyHoldProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91787.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyHoldProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a legacy hold profile score falls in. */
fun classifyLegacyHoldProfile(score: Double): String {
    if (score < 37.0) {
        return "critical"
    }
    if (score < 73.9) {
        return "ok"
    }
    return "medium"
}
