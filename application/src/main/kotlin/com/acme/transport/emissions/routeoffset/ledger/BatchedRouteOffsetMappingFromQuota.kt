package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.emissions.idling.quota.IdlingQuota
import com.acme.transport.emissions.idling.quota.classifyGlobalIdlingLedger

fun summarizeIdlingQuotasForMapping(items: MutableList<IdlingQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveBatchedRouteOffsetMappingsFromQuota(items: MutableList<IdlingQuota>): MutableList<BatchedRouteOffsetMapping> {
    val out = mutableListOf<BatchedRouteOffsetMapping>()
    for (item in items) {
        val derived = newBatchedRouteOffsetMapping(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateBatchedRouteOffsetMapping(item: BatchedRouteOffsetMapping): String {
    return classifyGlobalIdlingLedger(item.effectiveExposure())
}
