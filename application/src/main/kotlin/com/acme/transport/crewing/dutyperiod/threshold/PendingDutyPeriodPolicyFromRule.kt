package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.crewing.swap.score.SwapRule
import com.acme.transport.crewing.swap.score.swapScoreBucket

fun summarizeSwapRulesForPolicy(items: MutableList<SwapRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun derivePendingDutyPeriodPoliciesFromRule(items: MutableList<SwapRule>): MutableList<PendingDutyPeriodPolicy> {
    val out = mutableListOf<PendingDutyPeriodPolicy>()
    for (item in items) {
        val derived = newPendingDutyPeriodPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignPendingDutyPeriodPolicy(item: PendingDutyPeriodPolicy): String = swapScoreBucket(item.weightedLoad())
