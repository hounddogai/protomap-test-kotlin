package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun batchedDockDoorMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun batchedDockDoorMappingBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a batched dock door mapping score falls in. */
fun classifyBatchedDockDoorMapping(score: Double): String {
    if (score < 10.0) {
        return "critical"
    }
    if (score < 12.9) {
        return "ok"
    }
    if (score < 83.5) {
        return "low"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 18695.0, in order. */
fun batchedDockDoorMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 18695.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
