package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.linehaul.laneplan.bundle.AdaptiveLanePlanRecord
import com.acme.transport.linehaul.laneplan.bundle.classifyFallbackLanePlanSummary

fun summarizeAdaptiveLanePlanRecordsForRequest(items: MutableList<AdaptiveLanePlanRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveFallbackCapacityRequestsFromRecord(items: MutableList<AdaptiveLanePlanRecord>): MutableList<FallbackCapacityRequest> {
    val out = mutableListOf<FallbackCapacityRequest>()
    for (item in items) {
        val derived = newFallbackCapacityRequest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankFallbackCapacityRequest(item: FallbackCapacityRequest): String {
    return classifyFallbackLanePlanSummary(item.projectedExposure())
}
