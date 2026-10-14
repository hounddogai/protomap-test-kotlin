package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.lastmile.drop.rollup.RegionalDropTier
import com.acme.transport.lastmile.drop.rollup.tieredDropNoticeWeight

fun summarizeRegionalDropTiersForPolicy(items: MutableList<RegionalDropTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveWeightedParcelPoliciesFromTier(items: MutableList<RegionalDropTier>): MutableList<WeightedParcelPolicy> {
    val out = mutableListOf<WeightedParcelPolicy>()
    for (item in items) {
        val derived = newWeightedParcelPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreWeightedParcelPolicy(item: WeightedParcelPolicy): Double = tieredDropNoticeWeight(1, true)
