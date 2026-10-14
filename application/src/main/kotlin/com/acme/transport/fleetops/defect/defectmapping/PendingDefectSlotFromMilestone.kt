package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.fleetops.defect.defectcheckpoint.StrictDefectMilestone
import com.acme.transport.fleetops.defect.defectcheckpoint.classifyFallbackDefectDraft

fun summarizeStrictDefectMilestonesForSlot(items: MutableList<StrictDefectMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePendingDefectSlotsFromMilestone(items: MutableList<StrictDefectMilestone>): MutableList<PendingDefectSlot> {
    val out = mutableListOf<PendingDefectSlot>()
    for (item in items) {
        val derived = newPendingDefectSlot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computePendingDefectSlot(item: PendingDefectSlot): String = classifyFallbackDefectDraft(item.effectiveLoad())
