package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.linehaul.relay.relaygroup.BatchedRelayTarget
import com.acme.transport.linehaul.relay.relaygroup.lenientRelayGroupWeight

fun summarizeBatchedRelayTargetsForBatch(items: MutableList<BatchedRelayTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveBatchedCapacityBatchesFromTarget(items: MutableList<BatchedRelayTarget>): MutableList<BatchedCapacityBatch> {
    val out = mutableListOf<BatchedCapacityBatch>()
    for (item in items) {
        val derived = newBatchedCapacityBatch(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankBatchedCapacityBatch(item: BatchedCapacityBatch): Double = lenientRelayGroupWeight(2, true)
