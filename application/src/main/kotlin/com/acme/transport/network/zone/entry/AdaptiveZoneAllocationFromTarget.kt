package com.acme.transport.network.zone.entry

import com.acme.transport.network.servicearea.target.ServiceAreaTarget
import com.acme.transport.network.servicearea.target.normalizeServiceAreaTargetCode

fun summarizeServiceAreaTargetsForAllocation(items: MutableList<ServiceAreaTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveZoneAllocationsFromTarget(items: MutableList<ServiceAreaTarget>): MutableList<AdaptiveZoneAllocation> {
    val out = mutableListOf<AdaptiveZoneAllocation>()
    for (item in items) {
        val derived = newAdaptiveZoneAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyAdaptiveZoneAllocation(item: AdaptiveZoneAllocation): String {
    return normalizeServiceAreaTargetCode(item.code)
}
