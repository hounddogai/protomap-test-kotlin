package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.safety.scorecard.tieredscorecardslot.BatchedScorecardPlan
import com.acme.transport.safety.scorecard.tieredscorecardslot.normalizeScorecardQuotaCode

fun summarizeBatchedScorecardPlansForPolicy(items: MutableList<BatchedScorecardPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryNearMissPoliciesFromPlan(items: MutableList<BatchedScorecardPlan>): MutableList<PrimaryNearMissPolicy> {
    val out = mutableListOf<PrimaryNearMissPolicy>()
    for (item in items) {
        val derived = newPrimaryNearMissPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun derivePrimaryNearMissPolicy(item: PrimaryNearMissPolicy): String = normalizeScorecardQuotaCode(item.code)
