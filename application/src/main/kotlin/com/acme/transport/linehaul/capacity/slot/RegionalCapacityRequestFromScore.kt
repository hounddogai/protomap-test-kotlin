package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.linehaul.capacity.preference.CapacityScore
import com.acme.transport.linehaul.capacity.preference.classifyTieredCapacitySchedule

fun summarizeCapacityScoresForRequest(items: MutableList<CapacityScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveRegionalCapacityRequestsFromScore(items: MutableList<CapacityScore>): MutableList<RegionalCapacityRequest> {
    val out = mutableListOf<RegionalCapacityRequest>()
    for (item in items) {
        val derived = newRegionalCapacityRequest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateRegionalCapacityRequest(item: RegionalCapacityRequest): String {
    return classifyTieredCapacitySchedule(item.adjustedVolume())
}
