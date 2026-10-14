package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.customs.inspection.milestone.InspectionCheckpoint
import com.acme.transport.customs.inspection.milestone.normalizeInspectionMilestoneCode

fun summarizeInspectionCheckpointsForTemplate(items: MutableList<InspectionCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveClearanceTemplatesFromCheckpoint(items: MutableList<InspectionCheckpoint>): MutableList<ClearanceTemplate> {
    val out = mutableListOf<ClearanceTemplate>()
    for (item in items) {
        val derived = newClearanceTemplate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankClearanceTemplate(item: ClearanceTemplate): String = normalizeInspectionMilestoneCode(item.code)
