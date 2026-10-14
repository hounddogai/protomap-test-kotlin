package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.linehaul.relay.claim.AdaptiveRelayTemplate
import com.acme.transport.linehaul.relay.claim.seasonalRelayRequestWeight

fun summarizeAdaptiveRelayTemplatesForMilestone(items: MutableList<AdaptiveRelayTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveBackhaulMilestonesFromTemplate(items: MutableList<AdaptiveRelayTemplate>): MutableList<BackhaulMilestone> {
    val out = mutableListOf<BackhaulMilestone>()
    for (item in items) {
        val derived = newBackhaulMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankBackhaulMilestone(item: BackhaulMilestone): Double = seasonalRelayRequestWeight(6, true)
