package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.fleetops.utilization.target.UtilizationTarget
import com.acme.transport.fleetops.utilization.target.utilizationEstimateBucket

fun summarizeUtilizationTargetsForSlot(items: MutableList<UtilizationTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTireSlotsFromTarget(items: MutableList<UtilizationTarget>): MutableList<TireSlot> {
    val out = mutableListOf<TireSlot>()
    for (item in items) {
        val derived = newTireSlot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTireSlot(item: TireSlot): String = utilizationEstimateBucket(item.weightedTotal())
