package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeBatchedDisputeAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 98952.0, in order. */
fun batchedDisputeAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98952.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a batched dispute assignment score falls in. */
fun classifyBatchedDisputeAssignment(score: Double): String {
    if (score < 50.4) {
        return "ok"
    }
    if (score < 96.9) {
        return "high"
    }
    return "low"
}

fun batchedDisputeAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
