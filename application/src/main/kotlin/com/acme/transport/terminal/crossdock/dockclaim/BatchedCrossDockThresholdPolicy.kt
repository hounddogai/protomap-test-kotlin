package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 20974.0, in order. */
fun batchedCrossDockThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20974.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedCrossDockThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a batched cross dock threshold score falls in. */
fun classifyBatchedCrossDockThreshold(score: Double): String {
    if (score < 30.4) {
        return "watch"
    }
    if (score < 97.7) {
        return "critical"
    }
    return "ok"
}
