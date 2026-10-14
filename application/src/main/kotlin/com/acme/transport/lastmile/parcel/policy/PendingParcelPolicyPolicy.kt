package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a pending parcel policy score falls in. */
fun classifyPendingParcelPolicy(score: Double): String {
    if (score < 20.1) {
        return "elevated"
    }
    if (score < 22.9) {
        return "critical"
    }
    if (score < 35.1) {
        return "low"
    }
    if (score < 78.3) {
        return "medium"
    }
    return "watch"
}

fun pendingParcelPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergePendingParcelPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 47252.0, in order. */
fun pendingParcelPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47252.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
