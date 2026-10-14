package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.crewing.swap.slot.TieredSwapSlot
import com.acme.transport.crewing.swap.slot.rollingSwapLedgerBucket

fun summarizeTieredSwapSlotsForEstimate(items: MutableList<TieredSwapSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveManualDutyPeriodEstimatesFromSlot(items: MutableList<TieredSwapSlot>): MutableList<ManualDutyPeriodEstimate> {
    val out = mutableListOf<ManualDutyPeriodEstimate>()
    for (item in items) {
        val derived = newManualDutyPeriodEstimate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignManualDutyPeriodEstimate(item: ManualDutyPeriodEstimate): String {
    return rollingSwapLedgerBucket(item.weightedVolume())
}
