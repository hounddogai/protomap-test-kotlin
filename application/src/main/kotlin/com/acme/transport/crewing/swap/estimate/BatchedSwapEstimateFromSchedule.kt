package com.acme.transport.crewing.swap.estimate

import com.acme.transport.crewing.restbreak.adjustment.RestBreakSchedule
import com.acme.transport.crewing.restbreak.adjustment.normalizeScheduledRestBreakTargetCode

fun summarizeRestBreakSchedulesForEstimate(items: MutableList<RestBreakSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveBatchedSwapEstimatesFromSchedule(items: MutableList<RestBreakSchedule>): MutableList<BatchedSwapEstimate> {
    val out = mutableListOf<BatchedSwapEstimate>()
    for (item in items) {
        val derived = newBatchedSwapEstimate(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveBatchedSwapEstimate(item: BatchedSwapEstimate): String {
    return normalizeScheduledRestBreakTargetCode(item.code)
}
