package com.acme.transport.fleetops.tire.entry

import com.acme.transport.fleetops.inspection.summary.InspectionSummary
import com.acme.transport.fleetops.inspection.summary.classifyStrictInspectionProposal

fun summarizeInspectionSummariesForDraft(items: MutableList<InspectionSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun derivePrimaryTireDraftsFromSummary(items: MutableList<InspectionSummary>): MutableList<PrimaryTireDraft> {
    val out = mutableListOf<PrimaryTireDraft>()
    for (item in items) {
        val derived = newPrimaryTireDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settlePrimaryTireDraft(item: PrimaryTireDraft): String {
    return classifyStrictInspectionProposal(item.measuredValue())
}
