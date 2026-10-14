package com.acme.transport.customs.hold.request

import com.acme.transport.customs.tariff.tariffdigest.TariffWindow
import com.acme.transport.customs.tariff.tariffdigest.classifyAdaptiveTariffMetric

fun summarizeTariffWindowsForAllocation(items: MutableList<TariffWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedHoldAllocationsFromWindow(items: MutableList<TariffWindow>): MutableList<BatchedHoldAllocation> {
    val out = mutableListOf<BatchedHoldAllocation>()
    for (item in items) {
        val derived = newBatchedHoldAllocation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignBatchedHoldAllocation(item: BatchedHoldAllocation): String {
    return classifyAdaptiveTariffMetric(item.weightedTotal())
}
