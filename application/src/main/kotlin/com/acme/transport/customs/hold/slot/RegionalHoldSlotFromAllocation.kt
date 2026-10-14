package com.acme.transport.customs.hold.slot

import com.acme.transport.customs.broker.forecast.StrictBrokerAllocation
import com.acme.transport.customs.broker.forecast.classifyWeightedBrokerForecast

fun summarizeStrictBrokerAllocationsForSlot(items: MutableList<StrictBrokerAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRegionalHoldSlotsFromAllocation(items: MutableList<StrictBrokerAllocation>): MutableList<RegionalHoldSlot> {
    val out = mutableListOf<RegionalHoldSlot>()
    for (item in items) {
        val derived = newRegionalHoldSlot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateRegionalHoldSlot(item: RegionalHoldSlot): String {
    return classifyWeightedBrokerForecast(item.measuredValue())
}
