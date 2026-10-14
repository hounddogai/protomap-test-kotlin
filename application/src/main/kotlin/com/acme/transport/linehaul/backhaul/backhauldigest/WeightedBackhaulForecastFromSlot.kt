package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.linehaul.tender.tenderestimate.FallbackTenderSlot
import com.acme.transport.linehaul.tender.tenderestimate.normalizeTenderAuditCode

fun summarizeFallbackTenderSlotsForForecast(items: MutableList<FallbackTenderSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveWeightedBackhaulForecastsFromSlot(items: MutableList<FallbackTenderSlot>): MutableList<WeightedBackhaulForecast> {
    val out = mutableListOf<WeightedBackhaulForecast>()
    for (item in items) {
        val derived = newWeightedBackhaulForecast(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeWeightedBackhaulForecast(item: WeightedBackhaulForecast): String {
    return normalizeTenderAuditCode(item.code)
}
