package com.acme.transport.network.node.ledger

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 92973.0, in order. */
fun cappedNodeCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 92973.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedNodeCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun cappedNodeCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
