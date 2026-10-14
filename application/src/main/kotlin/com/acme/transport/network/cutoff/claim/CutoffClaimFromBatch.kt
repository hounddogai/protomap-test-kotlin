package com.acme.transport.network.cutoff.claim

import com.acme.transport.network.node.milestone.NodeBatch
import com.acme.transport.network.node.milestone.normalizeNodeThresholdCode

fun summarizeNodeBatchesForClaim(items: MutableList<NodeBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCutoffClaimsFromBatch(items: MutableList<NodeBatch>): MutableList<CutoffClaim> {
    val out = mutableListOf<CutoffClaim>()
    for (item in items) {
        val derived = newCutoffClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreCutoffClaim(item: CutoffClaim): String = normalizeNodeThresholdCode(item.code)
