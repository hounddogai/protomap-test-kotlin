package com.acme.transport.network.servicearea.threshold

import com.acme.transport.network.servicearea.request.FallbackServiceAreaAudit
import com.acme.transport.network.servicearea.request.adaptiveServiceAreaCheckpointBucket

fun summarizeFallbackServiceAreaAuditsForThreshold(items: MutableList<FallbackServiceAreaAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAdaptiveServiceAreaThresholdsFromAudit(items: MutableList<FallbackServiceAreaAudit>): MutableList<AdaptiveServiceAreaThreshold> {
    val out = mutableListOf<AdaptiveServiceAreaThreshold>()
    for (item in items) {
        val derived = newAdaptiveServiceAreaThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAdaptiveServiceAreaThreshold(item: AdaptiveServiceAreaThreshold): String {
    return adaptiveServiceAreaCheckpointBucket(item.netLoad())
}
