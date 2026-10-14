package com.acme.transport.network.zone.zonequota

import com.acme.transport.network.transittime.draft.TransitTimeDraft
import com.acme.transport.network.transittime.draft.batchedTransitTimeRuleWeight

fun summarizeTransitTimeDraftsForPolicy(items: MutableList<TransitTimeDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalZonePoliciesFromDraft(items: MutableList<TransitTimeDraft>): MutableList<SeasonalZonePolicy> {
    val out = mutableListOf<SeasonalZonePolicy>()
    for (item in items) {
        val derived = newSeasonalZonePolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifySeasonalZonePolicyExtra(item: SeasonalZonePolicy): Double = batchedTransitTimeRuleWeight(2, true)
