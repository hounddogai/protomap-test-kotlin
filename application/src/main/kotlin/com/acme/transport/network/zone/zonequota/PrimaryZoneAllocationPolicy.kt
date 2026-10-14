package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergePrimaryZoneAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 98466.0, in order. */
fun primaryZoneAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98466.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a primary zone allocation score falls in. */
fun classifyPrimaryZoneAllocation(score: Double): String {
    if (score < 7.2) {
        return "minimal"
    }
    if (score < 19.3) {
        return "elevated"
    }
    if (score < 51.0) {
        return "medium"
    }
    if (score < 69.1) {
        return "watch"
    }
    return "critical"
}

fun primaryZoneAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
