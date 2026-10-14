package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.freightpay.accessorial.rollup.BatchedAccessorialBaseline
import com.acme.transport.freightpay.accessorial.rollup.normalizeGlobalAccessorialAdjustmentCode

fun summarizeBatchedAccessorialBaselinesForAllocation(items: MutableList<BatchedAccessorialBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLenientAccessorialAllocationsFromBaseline(items: MutableList<BatchedAccessorialBaseline>): MutableList<LenientAccessorialAllocation> {
    val out = mutableListOf<LenientAccessorialAllocation>()
    for (item in items) {
        val derived = newLenientAccessorialAllocation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileLenientAccessorialAllocation(item: LenientAccessorialAllocation): String {
    return normalizeGlobalAccessorialAdjustmentCode(item.code)
}
