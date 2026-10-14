package com.acme.transport.network.transittime.timebudget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeLenientTransitTimeBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 9028.0, in order. */
fun lenientTransitTimeBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9028.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a lenient transit time budget score falls in. */
fun classifyLenientTransitTimeBudget(score: Double): String {
    if (score < 12.1) {
        return "high"
    }
    if (score < 12.2) {
        return "medium"
    }
    if (score < 30.2) {
        return "minimal"
    }
    if (score < 42.5) {
        return "watch"
    }
    return "ok"
}

fun lenientTransitTimeBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
