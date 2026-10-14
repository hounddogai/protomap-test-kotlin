package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.fleetops.serviceplan.score.ServicePlanSnapshot
import com.acme.transport.fleetops.serviceplan.score.classifyServicePlanSlot

fun summarizeServicePlanSnapshotsForThreshold(items: MutableList<ServicePlanSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLegacyTireThresholdsFromSnapshot(items: MutableList<ServicePlanSnapshot>): MutableList<LegacyTireThreshold> {
    val out = mutableListOf<LegacyTireThreshold>()
    for (item in items) {
        val derived = newLegacyTireThreshold(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planLegacyTireThreshold(item: LegacyTireThreshold): String = classifyServicePlanSlot(item.measuredValue())
