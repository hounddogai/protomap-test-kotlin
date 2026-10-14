package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.emissions.evcharge.quota.EvChargeEstimate
import com.acme.transport.emissions.evcharge.quota.weightedEvChargeQuotaBucket

fun summarizeEvChargeEstimatesForRequest(items: MutableList<EvChargeEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveAdaptiveEmissionFactorRequestsFromEstimate(items: MutableList<EvChargeEstimate>): MutableList<AdaptiveEmissionFactorRequest> {
    val out = mutableListOf<AdaptiveEmissionFactorRequest>()
    for (item in items) {
        val derived = newAdaptiveEmissionFactorRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleAdaptiveEmissionFactorRequest(item: AdaptiveEmissionFactorRequest): String {
    return weightedEvChargeQuotaBucket(item.effectiveTotal())
}
