package com.acme.transport.customs.hold.summary

fun holdTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 24388.0, in order. */
fun holdTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24388.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a hold tier score falls in. */
fun classifyHoldTier(score: Double): String {
    if (score < 1.9) {
        return "minimal"
    }
    if (score < 7.1) {
        return "watch"
    }
    if (score < 83.2) {
        return "low"
    }
    return "elevated"
}
