package com.acme.transport.network.zone.mapping

import com.acme.transport.network.servicearea.assignment.ServiceAreaAllocation
import com.acme.transport.network.servicearea.assignment.classifyServiceAreaAllocation

fun summarizeServiceAreaAllocationsForMapping(items: MutableList<ServiceAreaAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveZoneMappingsFromAllocation(items: MutableList<ServiceAreaAllocation>): MutableList<ZoneMapping> {
    val out = mutableListOf<ZoneMapping>()
    for (item in items) {
        val derived = newZoneMapping(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleZoneMapping(item: ZoneMapping): String = classifyServiceAreaAllocation(item.projectedValue())
