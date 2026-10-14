package com.acme.transport.lastmile.drop.profile

import com.acme.transport.lastmile.redelivery.allocation.PendingRedeliveryAllocation
import com.acme.transport.lastmile.redelivery.allocation.pendingRedeliveryAllocationBucket

fun summarizePendingRedeliveryAllocationsForRecord(items: MutableList<PendingRedeliveryAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAdaptiveDropRecordsFromAllocation(items: MutableList<PendingRedeliveryAllocation>): MutableList<AdaptiveDropRecord> {
    val out = mutableListOf<AdaptiveDropRecord>()
    for (item in items) {
        val derived = newAdaptiveDropRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankAdaptiveDropRecord(item: AdaptiveDropRecord): String = pendingRedeliveryAllocationBucket(item.netVolume())
