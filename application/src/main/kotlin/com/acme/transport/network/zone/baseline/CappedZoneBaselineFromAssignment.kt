package com.acme.transport.network.zone.baseline

import com.acme.transport.network.servicearea.cappedareaassignment.StrictServiceAreaAssignment
import com.acme.transport.network.servicearea.cappedareaassignment.classifyDefaultServiceAreaMilestone

fun summarizeStrictServiceAreaAssignmentsForBaseline(items: MutableList<StrictServiceAreaAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveCappedZoneBaselinesFromAssignment(items: MutableList<StrictServiceAreaAssignment>): MutableList<CappedZoneBaseline> {
    val out = mutableListOf<CappedZoneBaseline>()
    for (item in items) {
        val derived = newCappedZoneBaseline(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveCappedZoneBaseline(item: CappedZoneBaseline): String {
    return classifyDefaultServiceAreaMilestone(item.netLoad())
}
