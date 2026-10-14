package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.fleetops.inspection.segment.FallbackInspectionSegment
import com.acme.transport.fleetops.inspection.segment.fallbackInspectionSegmentBucket

fun summarizeFallbackInspectionSegmentsForLedger(items: MutableList<FallbackInspectionSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingLeaseReturnLedgersFromSegment(items: MutableList<FallbackInspectionSegment>): MutableList<RollingLeaseReturnLedger> {
    val out = mutableListOf<RollingLeaseReturnLedger>()
    for (item in items) {
        val derived = newRollingLeaseReturnLedger(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRollingLeaseReturnLedger(item: RollingLeaseReturnLedger): String {
    return fallbackInspectionSegmentBucket(item.effectiveVolume())
}
