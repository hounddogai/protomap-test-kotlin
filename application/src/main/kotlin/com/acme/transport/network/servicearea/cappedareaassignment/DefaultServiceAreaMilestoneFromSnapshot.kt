package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.network.hub.snapshot.HubSnapshot
import com.acme.transport.network.hub.snapshot.normalizeWeightedHubDraftCode

fun summarizeHubSnapshotsForMilestone(items: MutableList<HubSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveDefaultServiceAreaMilestonesFromSnapshot(items: MutableList<HubSnapshot>): MutableList<DefaultServiceAreaMilestone> {
    val out = mutableListOf<DefaultServiceAreaMilestone>()
    for (item in items) {
        val derived = newDefaultServiceAreaMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectDefaultServiceAreaMilestone(item: DefaultServiceAreaMilestone): String {
    return normalizeWeightedHubDraftCode(item.code)
}
