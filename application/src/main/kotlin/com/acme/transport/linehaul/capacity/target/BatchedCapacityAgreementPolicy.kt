package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeBatchedCapacityAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a batched capacity agreement score falls in. */
fun classifyBatchedCapacityAgreement(score: Double): String {
    if (score < 20.9) {
        return "high"
    }
    if (score < 29.9) {
        return "elevated"
    }
    if (score < 87.3) {
        return "minimal"
    }
    return "medium"
}

fun batchedCapacityAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 27605.0, in order. */
fun batchedCapacityAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27605.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
