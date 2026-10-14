package com.acme.transport.customs.hold.profile

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a adaptive hold target score falls in. */
fun classifyAdaptiveHoldTarget(score: Double): String {
    if (score < 5.3) {
        return "watch"
    }
    if (score < 32.8) {
        return "elevated"
    }
    if (score < 41.9) {
        return "medium"
    }
    if (score < 85.1) {
        return "ok"
    }
    return "high"
}

fun adaptiveHoldTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAdaptiveHoldTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 45240.0, in order. */
fun adaptiveHoldTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45240.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
