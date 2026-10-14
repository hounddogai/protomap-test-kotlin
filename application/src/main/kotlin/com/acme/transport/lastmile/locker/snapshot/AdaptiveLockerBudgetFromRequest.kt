package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.lastmile.drop.request.DropRequest
import com.acme.transport.lastmile.drop.request.normalizeDropBundleCode

fun summarizeDropRequestsForBudget(items: MutableList<DropRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveAdaptiveLockerBudgetsFromRequest(items: MutableList<DropRequest>): MutableList<AdaptiveLockerBudget> {
    val out = mutableListOf<AdaptiveLockerBudget>()
    for (item in items) {
        val derived = newAdaptiveLockerBudget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAdaptiveLockerBudget(item: AdaptiveLockerBudget): String = normalizeDropBundleCode(item.code)
