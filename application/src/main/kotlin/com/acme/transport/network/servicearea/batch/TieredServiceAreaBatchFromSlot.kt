package com.acme.transport.network.servicearea.batch

import com.acme.transport.network.servicearea.estimate.ServiceAreaSlot
import com.acme.transport.network.servicearea.estimate.normalizeServiceAreaEstimateCode

fun summarizeServiceAreaSlotsForBatch(items: MutableList<ServiceAreaSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveTieredServiceAreaBatchesFromSlot(items: MutableList<ServiceAreaSlot>): MutableList<TieredServiceAreaBatch> {
    val out = mutableListOf<TieredServiceAreaBatch>()
    for (item in items) {
        val derived = newTieredServiceAreaBatch(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeTieredServiceAreaBatch(item: TieredServiceAreaBatch): String {
    return normalizeServiceAreaEstimateCode(item.code)
}
