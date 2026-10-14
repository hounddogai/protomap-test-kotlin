package com.acme.transport.fleetops.tire.request

import com.acme.transport.fleetops.defect.budget.ScheduledDefectThreshold
import com.acme.transport.fleetops.defect.budget.normalizeScheduledDefectThresholdCode

fun summarizeScheduledDefectThresholdsForRequest(items: MutableList<ScheduledDefectThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun derivePrimaryTireRequestsFromThreshold(items: MutableList<ScheduledDefectThreshold>): MutableList<PrimaryTireRequest> {
    val out = mutableListOf<PrimaryTireRequest>()
    for (item in items) {
        val derived = newPrimaryTireRequest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimatePrimaryTireRequest(item: PrimaryTireRequest): String {
    return normalizeScheduledDefectThresholdCode(item.code)
}
