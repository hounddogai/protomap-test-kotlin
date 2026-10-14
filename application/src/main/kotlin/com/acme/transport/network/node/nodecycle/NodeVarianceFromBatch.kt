package com.acme.transport.network.node.nodecycle

import com.acme.transport.network.cutoff.batch.CutoffBatch
import com.acme.transport.network.cutoff.batch.cutoffBatchBucket

fun summarizeCutoffBatchesForVariance(items: MutableList<CutoffBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveNodeVariancesFromBatch(items: MutableList<CutoffBatch>): MutableList<NodeVariance> {
    val out = mutableListOf<NodeVariance>()
    for (item in items) {
        val derived = newNodeVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planNodeVariance(item: NodeVariance): String = cutoffBatchBucket(item.adjustedTotal())
