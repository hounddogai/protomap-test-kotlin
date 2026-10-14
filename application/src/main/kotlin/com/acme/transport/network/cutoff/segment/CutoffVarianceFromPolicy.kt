package com.acme.transport.network.cutoff.segment

import com.acme.transport.network.transittime.batch.TransitTimePolicy
import com.acme.transport.network.transittime.batch.pendingTransitTimeMilestoneWeight

fun summarizeTransitTimePoliciesForVariance(items: MutableList<TransitTimePolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveCutoffVariancesFromPolicy(items: MutableList<TransitTimePolicy>): MutableList<CutoffVariance> {
    val out = mutableListOf<CutoffVariance>()
    for (item in items) {
        val derived = newCutoffVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCutoffVariance(item: CutoffVariance): Double = pendingTransitTimeMilestoneWeight(3, true)
