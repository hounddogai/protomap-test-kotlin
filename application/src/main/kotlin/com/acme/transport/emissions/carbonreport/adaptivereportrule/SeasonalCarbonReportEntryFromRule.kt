package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.emissions.evcharge.assignment.EvChargeRule
import com.acme.transport.emissions.evcharge.assignment.scheduledEvChargeAssignmentBucket

fun summarizeEvChargeRulesForEntry(items: MutableList<EvChargeRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveSeasonalCarbonReportEntriesFromRule(items: MutableList<EvChargeRule>): MutableList<SeasonalCarbonReportEntry> {
    val out = mutableListOf<SeasonalCarbonReportEntry>()
    for (item in items) {
        val derived = newSeasonalCarbonReportEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileSeasonalCarbonReportEntry(item: SeasonalCarbonReportEntry): String {
    return scheduledEvChargeAssignmentBucket(item.netExposure())
}
