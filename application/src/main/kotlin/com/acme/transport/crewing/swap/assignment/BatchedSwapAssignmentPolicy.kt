package com.acme.transport.crewing.swap.assignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun batchedSwapAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a batched swap assignment score falls in. */
fun classifyBatchedSwapAssignment(score: Double): String {
    if (score < 38.9) {
        return "elevated"
    }
    if (score < 52.1) {
        return "low"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 92411.0, in order. */
fun batchedSwapAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 92411.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeBatchedSwapAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
