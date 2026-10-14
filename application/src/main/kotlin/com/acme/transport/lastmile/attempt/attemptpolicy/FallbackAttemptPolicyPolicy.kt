package com.acme.transport.lastmile.attempt.attemptpolicy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeFallbackAttemptPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 8919.0, in order. */
fun fallbackAttemptPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8919.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackAttemptPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
