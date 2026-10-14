package com.acme.transport.safety.scorecard.batch

import com.acme.transport.safety.scorecard.policy.ScheduledScorecardPolicy
import com.acme.transport.safety.scorecard.policy.classifyGlobalScorecardManifest

fun summarizeScheduledScorecardPoliciesForQuota(items: MutableList<ScheduledScorecardPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveTieredScorecardQuotasFromPolicy(items: MutableList<ScheduledScorecardPolicy>): MutableList<TieredScorecardQuota> {
    val out = mutableListOf<TieredScorecardQuota>()
    for (item in items) {
        val derived = newTieredScorecardQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateTieredScorecardQuota(item: TieredScorecardQuota): String {
    return classifyGlobalScorecardManifest(item.effectiveExposure())
}
