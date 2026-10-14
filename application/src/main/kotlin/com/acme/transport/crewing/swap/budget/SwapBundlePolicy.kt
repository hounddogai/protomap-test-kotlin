package com.acme.transport.crewing.swap.budget

/** Counts how many amounts fit in a budget of 99047.0, in order. */
fun swapBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99047.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a swap bundle score falls in. */
fun classifySwapBundle(score: Double): String {
    if (score < 27.4) {
        return "elevated"
    }
    if (score < 54.1) {
        return "watch"
    }
    return "critical"
}
