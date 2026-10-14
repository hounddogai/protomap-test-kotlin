package com.acme.transport.customs.inspection.review

import com.acme.transport.customs.hold.holdsummary.ScheduledHoldSummary
import com.acme.transport.customs.hold.holdsummary.normalizeLenientHoldScheduleCode

fun summarizeScheduledHoldSummariesForDraft(items: MutableList<ScheduledHoldSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRegionalInspectionDraftsFromSummary(items: MutableList<ScheduledHoldSummary>): MutableList<RegionalInspectionDraft> {
    val out = mutableListOf<RegionalInspectionDraft>()
    for (item in items) {
        val derived = newRegionalInspectionDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRegionalInspectionDraft(item: RegionalInspectionDraft): String {
    return normalizeLenientHoldScheduleCode(item.code)
}
