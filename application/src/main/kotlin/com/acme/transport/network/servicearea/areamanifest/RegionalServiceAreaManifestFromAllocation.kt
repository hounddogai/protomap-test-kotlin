package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.network.servicearea.assignment.ServiceAreaAllocation
import com.acme.transport.network.servicearea.assignment.classifyServiceAreaAllocation

fun summarizeServiceAreaAllocationsForManifest(items: MutableList<ServiceAreaAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalServiceAreaManifestsFromAllocation(items: MutableList<ServiceAreaAllocation>): MutableList<RegionalServiceAreaManifest> {
    val out = mutableListOf<RegionalServiceAreaManifest>()
    for (item in items) {
        val derived = newRegionalServiceAreaManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitRegionalServiceAreaManifest(item: RegionalServiceAreaManifest): String {
    return classifyServiceAreaAllocation(item.projectedVolume())
}
