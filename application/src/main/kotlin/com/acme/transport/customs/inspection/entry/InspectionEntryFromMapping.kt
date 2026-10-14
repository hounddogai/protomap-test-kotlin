package com.acme.transport.customs.inspection.entry

import com.acme.transport.customs.inspection.mapping.DefaultInspectionMapping
import com.acme.transport.customs.inspection.mapping.scheduledInspectionPlanWeight

fun summarizeDefaultInspectionMappingsForEntry(items: MutableList<DefaultInspectionMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveInspectionEntriesFromMapping(items: MutableList<DefaultInspectionMapping>): MutableList<InspectionEntry> {
    val out = mutableListOf<InspectionEntry>()
    for (item in items) {
        val derived = newInspectionEntry(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeInspectionEntry(item: InspectionEntry): Double = scheduledInspectionPlanWeight(3, true)
