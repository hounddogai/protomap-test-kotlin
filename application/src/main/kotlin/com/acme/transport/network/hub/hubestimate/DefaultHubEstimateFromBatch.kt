package com.acme.transport.network.hub.hubestimate

import com.acme.transport.network.node.request.LenientNodeBatch
import com.acme.transport.network.node.request.classifyBatchedNodeRequest

fun summarizeLenientNodeBatchesForEstimate(items: MutableList<LenientNodeBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultHubEstimatesFromBatch(items: MutableList<LenientNodeBatch>): MutableList<DefaultHubEstimate> {
    val out = mutableListOf<DefaultHubEstimate>()
    for (item in items) {
        val derived = newDefaultHubEstimate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDefaultHubEstimate(item: DefaultHubEstimate): String {
    return classifyBatchedNodeRequest(item.weightedTotal())
}
