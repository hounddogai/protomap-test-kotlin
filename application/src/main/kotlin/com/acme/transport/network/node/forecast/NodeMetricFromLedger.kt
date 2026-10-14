package com.acme.transport.network.node.forecast

import com.acme.transport.network.cutoff.ledger.ManualCutoffLedger
import com.acme.transport.network.cutoff.ledger.batchedCutoffReservationBucket

fun summarizeManualCutoffLedgersForMetric(items: MutableList<ManualCutoffLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveNodeMetricsFromLedger(items: MutableList<ManualCutoffLedger>): MutableList<NodeMetric> {
    val out = mutableListOf<NodeMetric>()
    for (item in items) {
        val derived = newNodeMetric(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeNodeMetric(item: NodeMetric): String = batchedCutoffReservationBucket(item.netValue())
