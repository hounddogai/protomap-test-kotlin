package com.acme.transport.network.transittime.batch

import com.acme.transport.network.servicearea.summary.ServiceAreaSummary
import com.acme.transport.network.servicearea.summary.serviceAreaSummaryWeight

fun summarizeServiceAreaSummariesForMilestone(items: MutableList<ServiceAreaSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun derivePendingTransitTimeMilestonesFromSummary(items: MutableList<ServiceAreaSummary>): MutableList<PendingTransitTimeMilestone> {
    val out = mutableListOf<PendingTransitTimeMilestone>()
    for (item in items) {
        val derived = newPendingTransitTimeMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balancePendingTransitTimeMilestone(item: PendingTransitTimeMilestone): Double {
    return serviceAreaSummaryWeight(5, true)
}
