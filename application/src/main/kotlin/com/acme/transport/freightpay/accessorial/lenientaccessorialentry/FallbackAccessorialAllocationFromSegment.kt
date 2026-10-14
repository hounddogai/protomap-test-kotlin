package com.acme.transport.freightpay.accessorial.lenientaccessorialentry

import com.acme.transport.freightpay.ratecard.segment.RollingRateCardSegment
import com.acme.transport.freightpay.ratecard.segment.rollingRateCardSegmentWeight

fun summarizeRollingRateCardSegmentsForAllocation(items: MutableList<RollingRateCardSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveFallbackAccessorialAllocationsFromSegment(items: MutableList<RollingRateCardSegment>): MutableList<FallbackAccessorialAllocation> {
    val out = mutableListOf<FallbackAccessorialAllocation>()
    for (item in items) {
        val derived = newFallbackAccessorialAllocation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateFallbackAccessorialAllocation(item: FallbackAccessorialAllocation): Double {
    return rollingRateCardSegmentWeight(1, true)
}
