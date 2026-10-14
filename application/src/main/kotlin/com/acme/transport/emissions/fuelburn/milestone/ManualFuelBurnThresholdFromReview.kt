package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.emissions.idling.estimate.IdlingReview
import com.acme.transport.emissions.idling.estimate.classifyIdlingReview

fun summarizeIdlingReviewsForThreshold(items: MutableList<IdlingReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveManualFuelBurnThresholdsFromReview(items: MutableList<IdlingReview>): MutableList<ManualFuelBurnThreshold> {
    val out = mutableListOf<ManualFuelBurnThreshold>()
    for (item in items) {
        val derived = newManualFuelBurnThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeManualFuelBurnThreshold(item: ManualFuelBurnThreshold): String {
    return classifyIdlingReview(item.effectiveExposure())
}
