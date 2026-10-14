package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.emissions.evcharge.profile.PendingEvChargeTarget
import com.acme.transport.emissions.evcharge.profile.normalizeGlobalEvChargeOverrideCode

fun summarizePendingEvChargeTargetsForReview(items: MutableList<PendingEvChargeTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveRegionalFuelBurnReviewsFromTarget(items: MutableList<PendingEvChargeTarget>): MutableList<RegionalFuelBurnReview> {
    val out = mutableListOf<RegionalFuelBurnReview>()
    for (item in items) {
        val derived = newRegionalFuelBurnReview(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleRegionalFuelBurnReview(item: RegionalFuelBurnReview): String {
    return normalizeGlobalEvChargeOverrideCode(item.code)
}
