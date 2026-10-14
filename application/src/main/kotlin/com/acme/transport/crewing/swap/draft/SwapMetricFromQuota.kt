package com.acme.transport.crewing.swap.draft

import com.acme.transport.crewing.swap.estimate.PendingSwapQuota
import com.acme.transport.crewing.swap.estimate.classifyBatchedSwapEstimate

fun summarizePendingSwapQuotasForMetric(items: MutableList<PendingSwapQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveSwapMetricsFromQuota(items: MutableList<PendingSwapQuota>): MutableList<SwapMetric> {
    val out = mutableListOf<SwapMetric>()
    for (item in items) {
        val derived = newSwapMetric(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateSwapMetric(item: SwapMetric): String = classifyBatchedSwapEstimate(item.measuredValue())
