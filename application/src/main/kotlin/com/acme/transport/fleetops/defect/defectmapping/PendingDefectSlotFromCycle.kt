package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.fleetops.utilization.cycle.ScheduledUtilizationCycle
import com.acme.transport.fleetops.utilization.cycle.scheduledUtilizationMetricWeight

fun summarizeScheduledUtilizationCyclesForSlot(items: MutableList<ScheduledUtilizationCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingDefectSlotsFromCycle(items: MutableList<ScheduledUtilizationCycle>): MutableList<PendingDefectSlot> {
    val out = mutableListOf<PendingDefectSlot>()
    for (item in items) {
        val derived = newPendingDefectSlot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planPendingDefectSlot(item: PendingDefectSlot): Double = scheduledUtilizationMetricWeight(1, true)
