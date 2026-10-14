package com.acme.transport.customs.clearance.metric

import com.acme.transport.customs.tariff.profile.TariffProfile
import com.acme.transport.customs.tariff.profile.normalizeTariffBaselineCode

fun summarizeTariffProfilesForMetric(items: MutableList<TariffProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveClearanceMetricsFromProfile(items: MutableList<TariffProfile>): MutableList<ClearanceMetric> {
    val out = mutableListOf<ClearanceMetric>()
    for (item in items) {
        val derived = newClearanceMetric(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchClearanceMetric(item: ClearanceMetric): String = normalizeTariffBaselineCode(item.code)
