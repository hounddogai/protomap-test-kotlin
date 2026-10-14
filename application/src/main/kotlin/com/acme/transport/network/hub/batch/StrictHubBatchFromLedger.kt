package com.acme.transport.network.hub.batch

import com.acme.transport.network.node.forecast.NodeLedger
import com.acme.transport.network.node.forecast.classifyNodeLedger

fun summarizeNodeLedgersForBatch(items: MutableList<NodeLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictHubBatchesFromLedger(items: MutableList<NodeLedger>): MutableList<StrictHubBatch> {
    val out = mutableListOf<StrictHubBatch>()
    for (item in items) {
        val derived = newStrictHubBatch(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleStrictHubBatch(item: StrictHubBatch): String = classifyNodeLedger(item.weightedLoad())
