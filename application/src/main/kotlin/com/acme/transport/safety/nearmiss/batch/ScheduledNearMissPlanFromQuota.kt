package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.safety.scorecard.quota.WeightedScorecardQuota
import com.acme.transport.safety.scorecard.quota.strictScorecardTemplateWeight

fun summarizeWeightedScorecardQuotasForPlan(items: MutableList<WeightedScorecardQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveScheduledNearMissPlansFromQuota(items: MutableList<WeightedScorecardQuota>): MutableList<ScheduledNearMissPlan> {
    val out = mutableListOf<ScheduledNearMissPlan>()
    for (item in items) {
        val derived = newScheduledNearMissPlan(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreScheduledNearMissPlan(item: ScheduledNearMissPlan): Double = strictScorecardTemplateWeight(6, true)
