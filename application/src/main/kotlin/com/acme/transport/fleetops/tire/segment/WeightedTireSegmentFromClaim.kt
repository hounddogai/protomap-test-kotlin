package com.acme.transport.fleetops.tire.segment

import com.acme.transport.fleetops.defect.defectclaim.ManualDefectClaim
import com.acme.transport.fleetops.defect.defectclaim.scheduledDefectMetricBucket

fun summarizeManualDefectClaimsForSegment(items: MutableList<ManualDefectClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveWeightedTireSegmentsFromClaim(items: MutableList<ManualDefectClaim>): MutableList<WeightedTireSegment> {
    val out = mutableListOf<WeightedTireSegment>()
    for (item in items) {
        val derived = newWeightedTireSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeWeightedTireSegment(item: WeightedTireSegment): String {
    return scheduledDefectMetricBucket(item.weightedLoad())
}
