package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.emissions.emissionfactor.digest.BatchedEmissionFactorDigest
import com.acme.transport.emissions.emissionfactor.digest.classifyEmissionFactorBundle

fun summarizeBatchedEmissionFactorDigestsForSummary(items: MutableList<BatchedEmissionFactorDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveGlobalRouteOffsetSummariesFromDigest(items: MutableList<BatchedEmissionFactorDigest>): MutableList<GlobalRouteOffsetSummary> {
    val out = mutableListOf<GlobalRouteOffsetSummary>()
    for (item in items) {
        val derived = newGlobalRouteOffsetSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateGlobalRouteOffsetSummary(item: GlobalRouteOffsetSummary): String {
    return classifyEmissionFactorBundle(item.adjustedExposure())
}
