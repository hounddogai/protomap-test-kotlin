package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.linehaul.relay.tier.DefaultRelayTemplate
import com.acme.transport.linehaul.relay.tier.classifyDefaultRelayTemplate

fun summarizeDefaultRelayTemplatesForSchedule(items: MutableList<DefaultRelayTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalCapacitySchedulesFromTemplate(items: MutableList<DefaultRelayTemplate>): MutableList<GlobalCapacitySchedule> {
    val out = mutableListOf<GlobalCapacitySchedule>()
    for (item in items) {
        val derived = newGlobalCapacitySchedule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeGlobalCapacitySchedule(item: GlobalCapacitySchedule): String {
    return classifyDefaultRelayTemplate(item.weightedLoad())
}
