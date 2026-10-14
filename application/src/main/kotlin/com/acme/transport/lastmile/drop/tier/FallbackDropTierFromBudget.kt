package com.acme.transport.lastmile.drop.tier

import com.acme.transport.lastmile.redelivery.budget.AdaptiveRedeliveryBudget
import com.acme.transport.lastmile.redelivery.budget.adaptiveRedeliveryBudgetBucket

fun summarizeAdaptiveRedeliveryBudgetsForTier(items: MutableList<AdaptiveRedeliveryBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveFallbackDropTiersFromBudget(items: MutableList<AdaptiveRedeliveryBudget>): MutableList<FallbackDropTier> {
    val out = mutableListOf<FallbackDropTier>()
    for (item in items) {
        val derived = newFallbackDropTier(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileFallbackDropTier(item: FallbackDropTier): String {
    return adaptiveRedeliveryBudgetBucket(item.effectiveExposure())
}
