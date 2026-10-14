package com.acme.transport.customs.hold.digest

import com.acme.transport.customs.clearance.seasonalclearanceoverride.GlobalClearanceOverride
import com.acme.transport.customs.clearance.seasonalclearanceoverride.classifyClearanceReservation

fun summarizeGlobalClearanceOverridesForSummary(items: MutableList<GlobalClearanceOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun derivePendingHoldSummariesFromOverride(items: MutableList<GlobalClearanceOverride>): MutableList<PendingHoldSummary> {
    val out = mutableListOf<PendingHoldSummary>()
    for (item in items) {
        val derived = newPendingHoldSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyPendingHoldSummary(item: PendingHoldSummary): String {
    return classifyClearanceReservation(item.projectedVolume())
}
