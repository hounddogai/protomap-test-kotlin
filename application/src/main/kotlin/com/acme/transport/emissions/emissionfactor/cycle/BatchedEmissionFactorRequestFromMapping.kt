package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.emissions.routeoffset.offsetmapping.FallbackRouteOffsetMapping
import com.acme.transport.emissions.routeoffset.offsetmapping.normalizeFallbackRouteOffsetMappingCode

fun summarizeFallbackRouteOffsetMappingsForRequest(items: MutableList<FallbackRouteOffsetMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveBatchedEmissionFactorRequestsFromMapping(items: MutableList<FallbackRouteOffsetMapping>): MutableList<BatchedEmissionFactorRequest> {
    val out = mutableListOf<BatchedEmissionFactorRequest>()
    for (item in items) {
        val derived = newBatchedEmissionFactorRequest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyBatchedEmissionFactorRequest(item: BatchedEmissionFactorRequest): String {
    return normalizeFallbackRouteOffsetMappingCode(item.code)
}
