package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.terminal.traileryard.agreement.GlobalTrailerYardMilestone
import com.acme.transport.terminal.traileryard.agreement.normalizeStrictTrailerYardTierCode

fun summarizeGlobalTrailerYardMilestonesForVariance(items: MutableList<GlobalTrailerYardMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveBatchedCrossDockVariancesFromMilestone(items: MutableList<GlobalTrailerYardMilestone>): MutableList<BatchedCrossDockVariance> {
    val out = mutableListOf<BatchedCrossDockVariance>()
    for (item in items) {
        val derived = newBatchedCrossDockVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectBatchedCrossDockVariance(item: BatchedCrossDockVariance): String {
    return normalizeStrictTrailerYardTierCode(item.code)
}
