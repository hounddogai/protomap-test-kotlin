package com.acme.transport.network.hub.allocation

import com.acme.transport.network.cutoff.globalcutoffledger.BatchedCutoffLedger
import com.acme.transport.network.cutoff.globalcutoffledger.normalizeBatchedCutoffLedgerCode

fun summarizeBatchedCutoffLedgersForPlan(items: MutableList<BatchedCutoffLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveWeightedHubPlansFromLedger(items: MutableList<BatchedCutoffLedger>): MutableList<WeightedHubPlan> {
    val out = mutableListOf<WeightedHubPlan>()
    for (item in items) {
        val derived = newWeightedHubPlan(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreWeightedHubPlan(item: WeightedHubPlan): String = normalizeBatchedCutoffLedgerCode(item.code)
