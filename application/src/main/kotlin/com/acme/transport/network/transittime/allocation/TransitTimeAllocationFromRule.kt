package com.acme.transport.network.transittime.allocation

import com.acme.transport.network.servicearea.areacycle.ScheduledServiceAreaRule
import com.acme.transport.network.servicearea.areacycle.scheduledServiceAreaRuleWeight

fun summarizeScheduledServiceAreaRulesForAllocation(items: MutableList<ScheduledServiceAreaRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTransitTimeAllocationsFromRule(items: MutableList<ScheduledServiceAreaRule>): MutableList<TransitTimeAllocation> {
    val out = mutableListOf<TransitTimeAllocation>()
    for (item in items) {
        val derived = newTransitTimeAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveTransitTimeAllocation(item: TransitTimeAllocation): Double = scheduledServiceAreaRuleWeight(9, true)
