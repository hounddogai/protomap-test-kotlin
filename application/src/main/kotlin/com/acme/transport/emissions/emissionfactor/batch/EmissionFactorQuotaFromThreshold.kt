package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.routeoffset.baseline.RouteOffsetThreshold
import com.acme.transport.emissions.routeoffset.baseline.routeOffsetAgreementBucket

fun summarizeRouteOffsetThresholdsForQuota(items: MutableList<RouteOffsetThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveEmissionFactorQuotasFromThreshold(items: MutableList<RouteOffsetThreshold>): MutableList<EmissionFactorQuota> {
    val out = mutableListOf<EmissionFactorQuota>()
    for (item in items) {
        val derived = newEmissionFactorQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeEmissionFactorQuota(item: EmissionFactorQuota): String {
    return routeOffsetAgreementBucket(item.adjustedLoad())
}
