package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.emissions.idling.idlingvariance.ManualIdlingAssignment
import com.acme.transport.emissions.idling.idlingvariance.classifyIdlingDraft

fun summarizeManualIdlingAssignmentsForMetric(items: MutableList<ManualIdlingAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveRollingRouteOffsetMetricsFromAssignment(items: MutableList<ManualIdlingAssignment>): MutableList<RollingRouteOffsetMetric> {
    val out = mutableListOf<RollingRouteOffsetMetric>()
    for (item in items) {
        val derived = newRollingRouteOffsetMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitRollingRouteOffsetMetric(item: RollingRouteOffsetMetric): String {
    return classifyIdlingDraft(item.weightedTotal())
}
