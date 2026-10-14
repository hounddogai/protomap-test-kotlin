package com.acme.transport.customs.inspection.digest

import com.acme.transport.customs.broker.estimate.BrokerEstimate
import com.acme.transport.customs.broker.estimate.brokerGroupBucket

fun summarizeBrokerEstimatesForDigest(items: MutableList<BrokerEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveInspectionDigestsFromEstimate(items: MutableList<BrokerEstimate>): MutableList<InspectionDigest> {
    val out = mutableListOf<InspectionDigest>()
    for (item in items) {
        val derived = newInspectionDigest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveInspectionDigest(item: InspectionDigest): String = brokerGroupBucket(item.weightedLoad())
