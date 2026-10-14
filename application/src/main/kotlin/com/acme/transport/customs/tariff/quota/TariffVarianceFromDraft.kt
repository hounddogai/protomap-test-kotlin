package com.acme.transport.customs.tariff.quota

import com.acme.transport.customs.clearance.clearanceoverride.ClearanceDraft
import com.acme.transport.customs.clearance.clearanceoverride.clearancePolicyWeight

fun summarizeClearanceDraftsForVariance(items: MutableList<ClearanceDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveTariffVariancesFromDraft(items: MutableList<ClearanceDraft>): MutableList<TariffVariance> {
    val out = mutableListOf<TariffVariance>()
    for (item in items) {
        val derived = newTariffVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planTariffVariance(item: TariffVariance): Double = clearancePolicyWeight(3, true)
