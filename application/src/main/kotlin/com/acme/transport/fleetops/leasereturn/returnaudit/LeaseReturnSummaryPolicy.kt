package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun leaseReturnSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a lease return summary score falls in. */
fun classifyLeaseReturnSummary(score: Double): String {
    if (score < 18.0) {
        return "ok"
    }
    if (score < 49.4) {
        return "high"
    }
    if (score < 77.1) {
        return "medium"
    }
    return "critical"
}

fun mergeLeaseReturnSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 19793.0, in order. */
fun leaseReturnSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19793.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
