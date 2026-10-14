package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.emissions.routeoffset.threshold.SeasonalRouteOffsetThreshold
import com.acme.transport.emissions.routeoffset.threshold.normalizeSeasonalRouteOffsetAdjustmentCode

fun summarizeSeasonalRouteOffsetThresholdsForQuota(items: MutableList<SeasonalRouteOffsetThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveManualIdlingQuotasFromThreshold(items: MutableList<SeasonalRouteOffsetThreshold>): MutableList<ManualIdlingQuota> {
    val out = mutableListOf<ManualIdlingQuota>()
    for (item in items) {
        val derived = newManualIdlingQuota(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignManualIdlingQuota(item: ManualIdlingQuota): String {
    return normalizeSeasonalRouteOffsetAdjustmentCode(item.code)
}
