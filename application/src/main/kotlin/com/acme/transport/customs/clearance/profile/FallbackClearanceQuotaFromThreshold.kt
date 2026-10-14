package com.acme.transport.customs.clearance.profile

import com.acme.transport.customs.tariff.score.TariffThreshold
import com.acme.transport.customs.tariff.score.tariffThresholdWeight

fun summarizeTariffThresholdsForQuota(items: MutableList<TariffThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveFallbackClearanceQuotasFromThreshold(items: MutableList<TariffThreshold>): MutableList<FallbackClearanceQuota> {
    val out = mutableListOf<FallbackClearanceQuota>()
    for (item in items) {
        val derived = newFallbackClearanceQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeFallbackClearanceQuota(item: FallbackClearanceQuota): Double = tariffThresholdWeight(5, true)
