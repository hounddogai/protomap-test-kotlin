package com.acme.transport.customs.hold.allocation

import com.acme.transport.customs.clearance.threshold.SeasonalClearanceProfile
import com.acme.transport.customs.clearance.threshold.clearanceThresholdBucket

fun summarizeSeasonalClearanceProfilesForReview(items: MutableList<SeasonalClearanceProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveHoldReviewsFromProfile(items: MutableList<SeasonalClearanceProfile>): MutableList<HoldReview> {
    val out = mutableListOf<HoldReview>()
    for (item in items) {
        val derived = newHoldReview(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchHoldReview(item: HoldReview): String = clearanceThresholdBucket(item.effectiveExposure())
