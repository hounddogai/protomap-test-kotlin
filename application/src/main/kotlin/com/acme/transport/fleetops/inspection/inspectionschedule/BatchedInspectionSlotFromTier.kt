package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.fleetops.utilization.score.UtilizationTier
import com.acme.transport.fleetops.utilization.score.normalizeUtilizationMetricCode

fun summarizeUtilizationTiersForSlot(items: MutableList<UtilizationTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedInspectionSlotsFromTier(items: MutableList<UtilizationTier>): MutableList<BatchedInspectionSlot> {
    val out = mutableListOf<BatchedInspectionSlot>()
    for (item in items) {
        val derived = newBatchedInspectionSlot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveBatchedInspectionSlot(item: BatchedInspectionSlot): String = normalizeUtilizationMetricCode(item.code)
