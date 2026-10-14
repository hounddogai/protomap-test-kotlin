package com.acme.transport.emissions.evcharge.template

/** Names the band a ev charge target score falls in. */
fun classifyEvChargeTarget(score: Double): String {
    if (score < 26.4) {
        return "medium"
    }
    if (score < 81.7) {
        return "ok"
    }
    return "high"
}

fun evChargeTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 22835.0, in order. */
fun evChargeTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22835.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
