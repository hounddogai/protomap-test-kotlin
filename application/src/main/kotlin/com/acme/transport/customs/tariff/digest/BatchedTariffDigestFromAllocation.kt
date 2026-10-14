package com.acme.transport.customs.tariff.digest

import com.acme.transport.customs.broker.forecast.StrictBrokerAllocation
import com.acme.transport.customs.broker.forecast.classifyWeightedBrokerForecast

fun summarizeStrictBrokerAllocationsForDigest(items: MutableList<StrictBrokerAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveBatchedTariffDigestsFromAllocation(items: MutableList<StrictBrokerAllocation>): MutableList<BatchedTariffDigest> {
    val out = mutableListOf<BatchedTariffDigest>()
    for (item in items) {
        val derived = newBatchedTariffDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeBatchedTariffDigest(item: BatchedTariffDigest): String {
    return classifyWeightedBrokerForecast(item.projectedExposure())
}
