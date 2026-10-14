package com.acme.transport.fleetops.tire.tier

import com.acme.transport.fleetops.inspection.inspectionbatch.CappedInspectionDraft
import com.acme.transport.fleetops.inspection.inspectionbatch.scheduledInspectionAgreementWeight

fun summarizeCappedInspectionDraftsForScore(items: MutableList<CappedInspectionDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveScheduledTireScoresFromDraft(items: MutableList<CappedInspectionDraft>): MutableList<ScheduledTireScore> {
    val out = mutableListOf<ScheduledTireScore>()
    for (item in items) {
        val derived = newScheduledTireScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyScheduledTireScore(item: ScheduledTireScore): Double = scheduledInspectionAgreementWeight(9, true)
