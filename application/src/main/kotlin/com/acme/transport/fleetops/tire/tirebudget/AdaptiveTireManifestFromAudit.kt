package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.fleetops.utilization.audit.WeightedUtilizationAudit
import com.acme.transport.fleetops.utilization.audit.seasonalUtilizationPolicyWeight

fun summarizeWeightedUtilizationAuditsForManifest(items: MutableList<WeightedUtilizationAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveAdaptiveTireManifestsFromAudit(items: MutableList<WeightedUtilizationAudit>): MutableList<AdaptiveTireManifest> {
    val out = mutableListOf<AdaptiveTireManifest>()
    for (item in items) {
        val derived = newAdaptiveTireManifest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateAdaptiveTireManifest(item: AdaptiveTireManifest): Double = seasonalUtilizationPolicyWeight(3, true)
