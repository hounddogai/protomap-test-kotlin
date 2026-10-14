package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.customs.hold.milestone.HoldEntry
import com.acme.transport.customs.hold.milestone.normalizeHoldEntryCode

fun summarizeHoldEntriesForCycle(items: MutableList<HoldEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveInspectionCyclesFromEntry(items: MutableList<HoldEntry>): MutableList<InspectionCycle> {
    val out = mutableListOf<InspectionCycle>()
    for (item in items) {
        val derived = newInspectionCycle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeInspectionCycle(item: InspectionCycle): String = normalizeHoldEntryCode(item.code)
