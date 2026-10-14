package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.network.hub.overrideitem.HubOverride
import com.acme.transport.network.hub.overrideitem.classifyHubProposal

fun summarizeHubOverridesForManifest(items: MutableList<HubOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRegionalServiceAreaManifestsFromOverride(items: MutableList<HubOverride>): MutableList<RegionalServiceAreaManifest> {
    val out = mutableListOf<RegionalServiceAreaManifest>()
    for (item in items) {
        val derived = newRegionalServiceAreaManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateRegionalServiceAreaManifest(item: RegionalServiceAreaManifest): String {
    return classifyHubProposal(item.projectedVolume())
}
