package com.acme.transport.network.cutoff.forecast

import com.acme.transport.network.zone.zonequota.SeasonalZoneAdjustment
import com.acme.transport.network.zone.zonequota.primaryZoneAllocationWeight

fun summarizeSeasonalZoneAdjustmentsForThreshold(items: MutableList<SeasonalZoneAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveCappedCutoffThresholdsFromAdjustment(items: MutableList<SeasonalZoneAdjustment>): MutableList<CappedCutoffThreshold> {
    val out = mutableListOf<CappedCutoffThreshold>()
    for (item in items) {
        val derived = newCappedCutoffThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchCappedCutoffThreshold(item: CappedCutoffThreshold): Double = primaryZoneAllocationWeight(7, true)
