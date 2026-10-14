package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 6050.0, in order. */
fun batchedRouteOffsetMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6050.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a batched route offset mapping score falls in. */
fun classifyBatchedRouteOffsetMapping(score: Double): String {
    if (score < 4.6) {
        return "watch"
    }
    if (score < 21.9) {
        return "critical"
    }
    if (score < 41.8) {
        return "low"
    }
    if (score < 91.4) {
        return "minimal"
    }
    return "medium"
}

fun batchedRouteOffsetMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
