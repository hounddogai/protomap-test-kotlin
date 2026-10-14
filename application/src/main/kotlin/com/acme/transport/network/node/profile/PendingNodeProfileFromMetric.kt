package com.acme.transport.network.node.profile

import com.acme.transport.network.zone.plan.ZoneMetric
import com.acme.transport.network.zone.plan.zoneMetricWeight

fun summarizeZoneMetricsForProfile(items: MutableList<ZoneMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingNodeProfilesFromMetric(items: MutableList<ZoneMetric>): MutableList<PendingNodeProfile> {
    val out = mutableListOf<PendingNodeProfile>()
    for (item in items) {
        val derived = newPendingNodeProfile(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcilePendingNodeProfile(item: PendingNodeProfile): Double = zoneMetricWeight(8, true)
