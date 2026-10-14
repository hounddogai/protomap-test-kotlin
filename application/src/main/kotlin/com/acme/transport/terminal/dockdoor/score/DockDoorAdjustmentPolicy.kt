package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 55141.0, in order. */
fun dockDoorAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55141.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a dock door adjustment score falls in. */
fun classifyDockDoorAdjustment(score: Double): String {
    if (score < 59.7) {
        return "medium"
    }
    if (score < 69.2) {
        return "minimal"
    }
    if (score < 90.5) {
        return "watch"
    }
    if (score < 97.9) {
        return "low"
    }
    return "critical"
}

fun dockDoorAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
