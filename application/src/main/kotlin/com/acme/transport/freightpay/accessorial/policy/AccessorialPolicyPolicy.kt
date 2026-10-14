package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun accessorialPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAccessorialPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a accessorial policy score falls in. */
fun classifyAccessorialPolicy(score: Double): String {
    if (score < 9.0) {
        return "minimal"
    }
    if (score < 37.3) {
        return "elevated"
    }
    if (score < 92.6) {
        return "critical"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 41952.0, in order. */
fun accessorialPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41952.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
