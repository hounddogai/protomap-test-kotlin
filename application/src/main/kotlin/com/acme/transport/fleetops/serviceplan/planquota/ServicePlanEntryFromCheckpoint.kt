package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.fleetops.utilization.plan.UtilizationCheckpoint
import com.acme.transport.fleetops.utilization.plan.utilizationCheckpointBucket

fun summarizeUtilizationCheckpointsForEntry(items: MutableList<UtilizationCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveServicePlanEntriesFromCheckpoint(items: MutableList<UtilizationCheckpoint>): MutableList<ServicePlanEntry> {
    val out = mutableListOf<ServicePlanEntry>()
    for (item in items) {
        val derived = newServicePlanEntry(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchServicePlanEntry(item: ServicePlanEntry): String = utilizationCheckpointBucket(item.projectedValue())
