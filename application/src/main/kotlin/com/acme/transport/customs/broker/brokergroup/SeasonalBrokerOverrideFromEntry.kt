package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.customs.inspection.entry.InspectionEntry
import com.acme.transport.customs.inspection.entry.seasonalInspectionBundleWeight

fun summarizeInspectionEntriesForOverride(items: MutableList<InspectionEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveSeasonalBrokerOverridesFromEntry(items: MutableList<InspectionEntry>): MutableList<SeasonalBrokerOverride> {
    val out = mutableListOf<SeasonalBrokerOverride>()
    for (item in items) {
        val derived = newSeasonalBrokerOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeSeasonalBrokerOverride(item: SeasonalBrokerOverride): Double {
    return seasonalInspectionBundleWeight(4, true)
}
