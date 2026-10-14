package com.acme.transport.network.node.slot

import com.acme.transport.network.transittime.mapping.TransitTimeMapping
import com.acme.transport.network.transittime.mapping.transitTimeMappingWeight

fun summarizeTransitTimeMappingsForNotice(items: MutableList<TransitTimeMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedNodeNoticesFromMapping(items: MutableList<TransitTimeMapping>): MutableList<BatchedNodeNotice> {
    val out = mutableListOf<BatchedNodeNotice>()
    for (item in items) {
        val derived = newBatchedNodeNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateBatchedNodeNotice(item: BatchedNodeNotice): Double = transitTimeMappingWeight(4, true)
