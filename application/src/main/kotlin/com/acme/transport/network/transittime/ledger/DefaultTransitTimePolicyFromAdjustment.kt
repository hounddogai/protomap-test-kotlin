package com.acme.transport.network.transittime.ledger

import com.acme.transport.network.cutoff.allocation.CutoffAdjustment
import com.acme.transport.network.cutoff.allocation.cutoffAdjustmentBucket

fun summarizeCutoffAdjustmentsForPolicy(items: MutableList<CutoffAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDefaultTransitTimePoliciesFromAdjustment(items: MutableList<CutoffAdjustment>): MutableList<DefaultTransitTimePolicy> {
    val out = mutableListOf<DefaultTransitTimePolicy>()
    for (item in items) {
        val derived = newDefaultTransitTimePolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignDefaultTransitTimePolicy(item: DefaultTransitTimePolicy): String {
    return cutoffAdjustmentBucket(item.weightedVolume())
}
