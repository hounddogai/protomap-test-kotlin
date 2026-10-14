package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.crewing.roster.budget.ManualRosterPlan
import com.acme.transport.crewing.roster.budget.classifyBatchedRosterTemplate

fun summarizeManualRosterPlansForTemplate(items: MutableList<ManualRosterPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveLegacyAvailabilityTemplatesFromPlan(items: MutableList<ManualRosterPlan>): MutableList<LegacyAvailabilityTemplate> {
    val out = mutableListOf<LegacyAvailabilityTemplate>()
    for (item in items) {
        val derived = newLegacyAvailabilityTemplate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLegacyAvailabilityTemplate(item: LegacyAvailabilityTemplate): String {
    return classifyBatchedRosterTemplate(item.measuredValue())
}
