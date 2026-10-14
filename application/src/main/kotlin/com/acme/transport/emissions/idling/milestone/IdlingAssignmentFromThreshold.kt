package com.acme.transport.emissions.idling.milestone

import com.acme.transport.emissions.idling.target.IdlingThreshold
import com.acme.transport.emissions.idling.target.idlingTargetBucket

fun summarizeIdlingThresholdsForAssignment(items: MutableList<IdlingThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveIdlingAssignmentsFromThreshold(items: MutableList<IdlingThreshold>): MutableList<IdlingAssignment> {
    val out = mutableListOf<IdlingAssignment>()
    for (item in items) {
        val derived = newIdlingAssignment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveIdlingAssignment(item: IdlingAssignment): String = idlingTargetBucket(item.effectiveLoad())
