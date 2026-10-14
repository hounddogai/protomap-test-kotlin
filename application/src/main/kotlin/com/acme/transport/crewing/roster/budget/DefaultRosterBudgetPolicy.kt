package com.acme.transport.crewing.roster.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun defaultRosterBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeDefaultRosterBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a default roster budget score falls in. */
fun classifyDefaultRosterBudget(score: Double): String {
    if (score < 19.7) {
        return "elevated"
    }
    if (score < 48.2) {
        return "low"
    }
    if (score < 96.1) {
        return "medium"
    }
    return "ok"
}

fun defaultRosterBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
