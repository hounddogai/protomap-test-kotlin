package com.acme.transport.customs.tariff.policy

import com.acme.transport.customs.clearance.tier.ClearanceTier
import com.acme.transport.customs.clearance.tier.clearanceEstimateWeight

fun summarizeClearanceTiersForPolicy(items: MutableList<ClearanceTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTariffPoliciesFromTier(items: MutableList<ClearanceTier>): MutableList<TariffPolicy> {
    val out = mutableListOf<TariffPolicy>()
    for (item in items) {
        val derived = newTariffPolicy(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankTariffPolicy(item: TariffPolicy): Double = clearanceEstimateWeight(8, true)
