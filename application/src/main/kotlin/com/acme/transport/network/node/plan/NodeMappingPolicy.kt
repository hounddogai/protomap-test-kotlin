package com.acme.transport.network.node.plan

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 83483.0, in order. */
fun nodeMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83483.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun nodeMappingBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
