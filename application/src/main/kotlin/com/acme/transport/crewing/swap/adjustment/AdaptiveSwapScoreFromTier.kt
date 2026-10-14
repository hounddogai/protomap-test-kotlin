package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.crewing.availability.availabilityreservation.AdaptiveAvailabilityTier
import com.acme.transport.crewing.availability.availabilityreservation.normalizeCappedAvailabilityBudgetCode

fun summarizeAdaptiveAvailabilityTiersForScore(items: MutableList<AdaptiveAvailabilityTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveAdaptiveSwapScoresFromTier(items: MutableList<AdaptiveAvailabilityTier>): MutableList<AdaptiveSwapScore> {
    val out = mutableListOf<AdaptiveSwapScore>()
    for (item in items) {
        val derived = newAdaptiveSwapScore(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceAdaptiveSwapScore(item: AdaptiveSwapScore): String = normalizeCappedAvailabilityBudgetCode(item.code)
