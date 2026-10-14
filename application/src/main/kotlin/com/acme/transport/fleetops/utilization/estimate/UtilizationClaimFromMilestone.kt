package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.fleetops.tire.milestone.TireMilestone
import com.acme.transport.fleetops.tire.milestone.normalizeTireMilestoneCode

fun summarizeTireMilestonesForClaim(items: MutableList<TireMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveUtilizationClaimsFromMilestone(items: MutableList<TireMilestone>): MutableList<UtilizationClaim> {
    val out = mutableListOf<UtilizationClaim>()
    for (item in items) {
        val derived = newUtilizationClaim(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeUtilizationClaim(item: UtilizationClaim): String = normalizeTireMilestoneCode(item.code)
