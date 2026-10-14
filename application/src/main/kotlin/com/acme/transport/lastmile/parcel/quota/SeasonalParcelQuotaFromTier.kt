package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.lastmile.redelivery.budget.FallbackRedeliveryTier
import com.acme.transport.lastmile.redelivery.budget.adaptiveRedeliveryBudgetBucket

fun summarizeFallbackRedeliveryTiersForQuota(items: MutableList<FallbackRedeliveryTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveSeasonalParcelQuotasFromTier(items: MutableList<FallbackRedeliveryTier>): MutableList<SeasonalParcelQuota> {
    val out = mutableListOf<SeasonalParcelQuota>()
    for (item in items) {
        val derived = newSeasonalParcelQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeSeasonalParcelQuota(item: SeasonalParcelQuota): String {
    return adaptiveRedeliveryBudgetBucket(item.weightedExposure())
}
