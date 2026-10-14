package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.tire.reservation.TireGroup
import com.acme.transport.fleetops.tire.reservation.normalizeSeasonalTireMilestoneCode

fun summarizeTireGroupsForScore(items: MutableList<TireGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveWeightedTireScoresFromGroup(items: MutableList<TireGroup>): MutableList<WeightedTireScore> {
    val out = mutableListOf<WeightedTireScore>()
    for (item in items) {
        val derived = newWeightedTireScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateWeightedTireScore(item: WeightedTireScore): String = normalizeSeasonalTireMilestoneCode(item.code)
