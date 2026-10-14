package com.acme.transport.customs.tariff.notice

import com.acme.transport.customs.clearance.clearancethreshold.LenientClearanceThreshold
import com.acme.transport.customs.clearance.clearancethreshold.manualClearancePlanWeight

fun summarizeLenientClearanceThresholdsForQuota(items: MutableList<LenientClearanceThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveLegacyTariffQuotasFromThreshold(items: MutableList<LenientClearanceThreshold>): MutableList<LegacyTariffQuota> {
    val out = mutableListOf<LegacyTariffQuota>()
    for (item in items) {
        val derived = newLegacyTariffQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankLegacyTariffQuota(item: LegacyTariffQuota): Double = manualClearancePlanWeight(7, true)
