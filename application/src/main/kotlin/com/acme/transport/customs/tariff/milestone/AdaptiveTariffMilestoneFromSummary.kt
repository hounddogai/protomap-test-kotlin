package com.acme.transport.customs.tariff.milestone

import com.acme.transport.customs.clearance.reservation.ClearanceSummary
import com.acme.transport.customs.clearance.reservation.defaultClearanceBaselineWeight

fun summarizeClearanceSummariesForMilestone(items: MutableList<ClearanceSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveAdaptiveTariffMilestonesFromSummary(items: MutableList<ClearanceSummary>): MutableList<AdaptiveTariffMilestone> {
    val out = mutableListOf<AdaptiveTariffMilestone>()
    for (item in items) {
        val derived = newAdaptiveTariffMilestone(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyAdaptiveTariffMilestone(item: AdaptiveTariffMilestone): Double = defaultClearanceBaselineWeight(9, true)
