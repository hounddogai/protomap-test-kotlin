package com.acme.transport.crewing.qualification.metric

import com.acme.transport.crewing.swap.rule.SwapLedger
import com.acme.transport.crewing.swap.rule.tieredSwapRuleBucket

fun summarizeSwapLedgersForCheckpoint(items: MutableList<SwapLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveGlobalQualificationCheckpointsFromLedger(items: MutableList<SwapLedger>): MutableList<GlobalQualificationCheckpoint> {
    val out = mutableListOf<GlobalQualificationCheckpoint>()
    for (item in items) {
        val derived = newGlobalQualificationCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeGlobalQualificationCheckpoint(item: GlobalQualificationCheckpoint): String {
    return tieredSwapRuleBucket(item.measuredValue())
}
