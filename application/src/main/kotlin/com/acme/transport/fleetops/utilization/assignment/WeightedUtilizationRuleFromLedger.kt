package com.acme.transport.fleetops.utilization.assignment

import com.acme.transport.fleetops.serviceplan.proposal.RollingServicePlanLedger
import com.acme.transport.fleetops.serviceplan.proposal.servicePlanProposalBucket

fun summarizeRollingServicePlanLedgersForRule(items: MutableList<RollingServicePlanLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveWeightedUtilizationRulesFromLedger(items: MutableList<RollingServicePlanLedger>): MutableList<WeightedUtilizationRule> {
    val out = mutableListOf<WeightedUtilizationRule>()
    for (item in items) {
        val derived = newWeightedUtilizationRule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeWeightedUtilizationRule(item: WeightedUtilizationRule): String {
    return servicePlanProposalBucket(item.measuredValue())
}
