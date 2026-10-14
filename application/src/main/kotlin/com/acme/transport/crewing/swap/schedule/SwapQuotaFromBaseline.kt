package com.acme.transport.crewing.swap.schedule

import com.acme.transport.crewing.swap.baseline.SwapBaseline
import com.acme.transport.crewing.swap.baseline.swapWindowBucket

fun summarizeSwapBaselinesForQuota(items: MutableList<SwapBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSwapQuotasFromBaseline(items: MutableList<SwapBaseline>): MutableList<SwapQuota> {
    val out = mutableListOf<SwapQuota>()
    for (item in items) {
        val derived = newSwapQuota(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreSwapQuota(item: SwapQuota): String = swapWindowBucket(item.measuredValue())
