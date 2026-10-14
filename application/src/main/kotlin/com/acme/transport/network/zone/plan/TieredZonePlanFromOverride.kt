package com.acme.transport.network.zone.plan

import com.acme.transport.network.zone.overrideitem.LegacyZoneOverride
import com.acme.transport.network.zone.overrideitem.legacyZoneOverrideWeight

fun summarizeLegacyZoneOverridesForPlan(items: MutableList<LegacyZoneOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveTieredZonePlansFromOverride(items: MutableList<LegacyZoneOverride>): MutableList<TieredZonePlan> {
    val out = mutableListOf<TieredZonePlan>()
    for (item in items) {
        val derived = newTieredZonePlan(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateTieredZonePlan(item: TieredZonePlan): Double = legacyZoneOverrideWeight(9, true)
