package com.acme.transport.network.node.tier

import com.acme.transport.network.zone.batchedzonemapping.StrictZoneCycle
import com.acme.transport.network.zone.batchedzonemapping.scheduledZoneSegmentBucket

fun summarizeStrictZoneCyclesForBaseline(items: MutableList<StrictZoneCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveBatchedNodeBaselinesFromCycle(items: MutableList<StrictZoneCycle>): MutableList<BatchedNodeBaseline> {
    val out = mutableListOf<BatchedNodeBaseline>()
    for (item in items) {
        val derived = newBatchedNodeBaseline(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeBatchedNodeBaseline(item: BatchedNodeBaseline): String {
    return scheduledZoneSegmentBucket(item.measuredValue())
}
