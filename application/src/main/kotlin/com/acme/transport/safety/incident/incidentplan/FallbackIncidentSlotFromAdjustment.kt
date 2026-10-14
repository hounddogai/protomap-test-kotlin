package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.safety.nearmiss.estimate.RollingNearMissAdjustment
import com.acme.transport.safety.nearmiss.estimate.scheduledNearMissBudgetWeight

fun summarizeRollingNearMissAdjustmentsForSlot(items: MutableList<RollingNearMissAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveFallbackIncidentSlotsFromAdjustment(items: MutableList<RollingNearMissAdjustment>): MutableList<FallbackIncidentSlot> {
    val out = mutableListOf<FallbackIncidentSlot>()
    for (item in items) {
        val derived = newFallbackIncidentSlot(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileFallbackIncidentSlot(item: FallbackIncidentSlot): Double = scheduledNearMissBudgetWeight(9, true)
