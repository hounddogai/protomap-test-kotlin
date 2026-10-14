package com.acme.transport.network.zone.zonetier

import com.acme.transport.network.hub.plan.HubPlan
import com.acme.transport.network.hub.plan.normalizeTieredHubBaselineCode

fun summarizeHubPlansForScore(items: MutableList<HubPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveZoneScoresFromPlan(items: MutableList<HubPlan>): MutableList<ZoneScore> {
    val out = mutableListOf<ZoneScore>()
    for (item in items) {
        val derived = newZoneScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleZoneScore(item: ZoneScore): String = normalizeTieredHubBaselineCode(item.code)
