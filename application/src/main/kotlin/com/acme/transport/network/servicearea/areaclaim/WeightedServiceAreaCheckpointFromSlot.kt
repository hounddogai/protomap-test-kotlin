package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.network.hub.template.FallbackHubSlot
import com.acme.transport.network.hub.template.normalizeRegionalHubNoticeCode

fun summarizeFallbackHubSlotsForCheckpoint(items: MutableList<FallbackHubSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveWeightedServiceAreaCheckpointsFromSlot(items: MutableList<FallbackHubSlot>): MutableList<WeightedServiceAreaCheckpoint> {
    val out = mutableListOf<WeightedServiceAreaCheckpoint>()
    for (item in items) {
        val derived = newWeightedServiceAreaCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileWeightedServiceAreaCheckpoint(item: WeightedServiceAreaCheckpoint): String {
    return normalizeRegionalHubNoticeCode(item.code)
}
