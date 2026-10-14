package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.utilization.plan.FallbackUtilizationPlan
import com.acme.transport.fleetops.utilization.plan.utilizationCheckpointBucket

fun summarizeFallbackUtilizationPlansForCheckpoint(items: MutableList<FallbackUtilizationPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRollingTireCheckpointsFromPlan(items: MutableList<FallbackUtilizationPlan>): MutableList<RollingTireCheckpoint> {
    val out = mutableListOf<RollingTireCheckpoint>()
    for (item in items) {
        val derived = newRollingTireCheckpoint(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignRollingTireCheckpoint(item: RollingTireCheckpoint): String {
    return utilizationCheckpointBucket(item.adjustedVolume())
}
