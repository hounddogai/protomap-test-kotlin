package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.linehaul.backhaul.backhaulallocation.AdaptiveBackhaulMilestone
import com.acme.transport.linehaul.backhaul.backhaulallocation.classifyFallbackBackhaulReservation

fun summarizeAdaptiveBackhaulMilestonesForBaseline(items: MutableList<AdaptiveBackhaulMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRelayBaselinesFromMilestone(items: MutableList<AdaptiveBackhaulMilestone>): MutableList<RelayBaseline> {
    val out = mutableListOf<RelayBaseline>()
    for (item in items) {
        val derived = newRelayBaseline(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyRelayBaseline(item: RelayBaseline): String = classifyFallbackBackhaulReservation(item.adjustedVolume())
