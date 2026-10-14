package com.acme.transport.customs.hold.estimate

import com.acme.transport.customs.broker.checkpoint.CappedBrokerCheckpoint
import com.acme.transport.customs.broker.checkpoint.cappedBrokerMappingWeight

fun summarizeCappedBrokerCheckpointsForEstimate(items: MutableList<CappedBrokerCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveTieredHoldEstimatesFromCheckpoint(items: MutableList<CappedBrokerCheckpoint>): MutableList<TieredHoldEstimate> {
    val out = mutableListOf<TieredHoldEstimate>()
    for (item in items) {
        val derived = newTieredHoldEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyTieredHoldEstimate(item: TieredHoldEstimate): Double = cappedBrokerMappingWeight(6, true)
