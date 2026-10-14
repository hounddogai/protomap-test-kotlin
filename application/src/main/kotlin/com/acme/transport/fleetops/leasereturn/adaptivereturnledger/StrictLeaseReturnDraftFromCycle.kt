package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.fleetops.utilization.cycle.ScheduledUtilizationCycle
import com.acme.transport.fleetops.utilization.cycle.classifyScheduledUtilizationMetric

fun summarizeScheduledUtilizationCyclesForDraft(items: MutableList<ScheduledUtilizationCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictLeaseReturnDraftsFromCycle(items: MutableList<ScheduledUtilizationCycle>): MutableList<StrictLeaseReturnDraft> {
    val out = mutableListOf<StrictLeaseReturnDraft>()
    for (item in items) {
        val derived = newStrictLeaseReturnDraft(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeStrictLeaseReturnDraft(item: StrictLeaseReturnDraft): String {
    return classifyScheduledUtilizationMetric(item.weightedVolume())
}
