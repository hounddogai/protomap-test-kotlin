package com.acme.transport.fleetops.tire.tirereservation

import com.acme.transport.fleetops.utilization.defaultutilizationsummary.UtilizationEntry
import com.acme.transport.fleetops.utilization.defaultutilizationsummary.classifyDefaultUtilizationPolicy

fun summarizeUtilizationEntriesForProposal(items: MutableList<UtilizationEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRegionalTireProposalsFromEntry(items: MutableList<UtilizationEntry>): MutableList<RegionalTireProposal> {
    val out = mutableListOf<RegionalTireProposal>()
    for (item in items) {
        val derived = newRegionalTireProposal(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileRegionalTireProposal(item: RegionalTireProposal): String {
    return classifyDefaultUtilizationPolicy(item.weightedTotal())
}
