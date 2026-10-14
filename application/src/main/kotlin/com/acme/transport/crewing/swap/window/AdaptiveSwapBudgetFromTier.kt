package com.acme.transport.crewing.swap.window

import com.acme.transport.crewing.availability.tier.AvailabilityTier
import com.acme.transport.crewing.availability.tier.availabilityManifestBucket

fun summarizeAvailabilityTiersForBudget(items: MutableList<AvailabilityTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveAdaptiveSwapBudgetsFromTier(items: MutableList<AvailabilityTier>): MutableList<AdaptiveSwapBudget> {
    val out = mutableListOf<AdaptiveSwapBudget>()
    for (item in items) {
        val derived = newAdaptiveSwapBudget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreAdaptiveSwapBudget(item: AdaptiveSwapBudget): String = availabilityManifestBucket(item.projectedTotal())
