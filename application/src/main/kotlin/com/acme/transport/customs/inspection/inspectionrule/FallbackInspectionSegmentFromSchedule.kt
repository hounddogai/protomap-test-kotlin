package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.customs.inspection.entry.LegacyInspectionSchedule
import com.acme.transport.customs.inspection.entry.normalizeInspectionEntryCode

fun summarizeLegacyInspectionSchedulesForSegment(items: MutableList<LegacyInspectionSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveFallbackInspectionSegmentsFromSchedule(items: MutableList<LegacyInspectionSchedule>): MutableList<FallbackInspectionSegment> {
    val out = mutableListOf<FallbackInspectionSegment>()
    for (item in items) {
        val derived = newFallbackInspectionSegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeFallbackInspectionSegment(item: FallbackInspectionSegment): String {
    return normalizeInspectionEntryCode(item.code)
}
