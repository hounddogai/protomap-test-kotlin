package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.emissions.evcharge.proposal.CappedEvChargeVariance
import com.acme.transport.emissions.evcharge.proposal.tieredEvChargeProposalWeight

fun summarizeCappedEvChargeVariancesForDigest(items: MutableList<CappedEvChargeVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveIdlingDigestsFromVariance(items: MutableList<CappedEvChargeVariance>): MutableList<IdlingDigest> {
    val out = mutableListOf<IdlingDigest>()
    for (item in items) {
        val derived = newIdlingDigest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignIdlingDigest(item: IdlingDigest): Double = tieredEvChargeProposalWeight(5, true)
