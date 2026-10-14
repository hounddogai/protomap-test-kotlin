package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.numbers.roundTo

fun pendingCoachingTemplateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a pending coaching template score falls in. */
fun classifyPendingCoachingTemplate(score: Double): String {
    if (score < 15.9) {
        return "watch"
    }
    if (score < 29.0) {
        return "medium"
    }
    if (score < 34.6) {
        return "minimal"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 15833.0, in order. */
fun pendingCoachingTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15833.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
