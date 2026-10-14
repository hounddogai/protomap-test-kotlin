package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a inspection budget score falls in. */
fun classifyInspectionBudget(score: Double): String {
    if (score < 48.5) {
        return "low"
    }
    if (score < 58.0) {
        return "watch"
    }
    if (score < 63.5) {
        return "ok"
    }
    return "medium"
}

fun inspectionBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeInspectionBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
