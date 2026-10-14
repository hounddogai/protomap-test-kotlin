package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.freightpay.accessorial.rollup.BatchedAccessorialRollup
import com.acme.transport.freightpay.accessorial.rollup.normalizeGlobalAccessorialAdjustmentCode

fun summarizeBatchedAccessorialRollupsForRecord(items: MutableList<BatchedAccessorialRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveFallbackDisputeRecordsFromRollup(items: MutableList<BatchedAccessorialRollup>): MutableList<FallbackDisputeRecord> {
    val out = mutableListOf<FallbackDisputeRecord>()
    for (item in items) {
        val derived = newFallbackDisputeRecord(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitFallbackDisputeRecord(item: FallbackDisputeRecord): String {
    return normalizeGlobalAccessorialAdjustmentCode(item.code)
}
