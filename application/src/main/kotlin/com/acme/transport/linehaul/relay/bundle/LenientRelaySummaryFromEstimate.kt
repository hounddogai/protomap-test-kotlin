package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.linehaul.capacity.ledger.SeasonalCapacityEstimate
import com.acme.transport.linehaul.capacity.ledger.adaptiveCapacityPolicyWeight

fun summarizeSeasonalCapacityEstimatesForSummary(items: MutableList<SeasonalCapacityEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveLenientRelaySummariesFromEstimate(items: MutableList<SeasonalCapacityEstimate>): MutableList<LenientRelaySummary> {
    val out = mutableListOf<LenientRelaySummary>()
    for (item in items) {
        val derived = newLenientRelaySummary(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLenientRelaySummary(item: LenientRelaySummary): Double = adaptiveCapacityPolicyWeight(2, true)
