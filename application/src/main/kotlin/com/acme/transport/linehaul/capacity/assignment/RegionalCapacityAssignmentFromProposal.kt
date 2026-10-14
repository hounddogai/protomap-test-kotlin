package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.linehaul.backhaul.quota.BackhaulProposal
import com.acme.transport.linehaul.backhaul.quota.classifyRollingBackhaulQuota

fun summarizeBackhaulProposalsForAssignment(items: MutableList<BackhaulProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveRegionalCapacityAssignmentsFromProposal(items: MutableList<BackhaulProposal>): MutableList<RegionalCapacityAssignment> {
    val out = mutableListOf<RegionalCapacityAssignment>()
    for (item in items) {
        val derived = newRegionalCapacityAssignment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeRegionalCapacityAssignment(item: RegionalCapacityAssignment): String {
    return classifyRollingBackhaulQuota(item.effectiveExposure())
}
