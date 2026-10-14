package com.acme.transport.customs.broker.exception

import com.acme.transport.customs.hold.group.RollingHoldAllocation
import com.acme.transport.customs.hold.group.adaptiveHoldTemplateWeight

fun summarizeRollingHoldAllocationsForVariance(items: MutableList<RollingHoldAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveRegionalBrokerVariancesFromAllocation(items: MutableList<RollingHoldAllocation>): MutableList<RegionalBrokerVariance> {
    val out = mutableListOf<RegionalBrokerVariance>()
    for (item in items) {
        val derived = newRegionalBrokerVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeRegionalBrokerVariance(item: RegionalBrokerVariance): Double = adaptiveHoldTemplateWeight(1, true)
