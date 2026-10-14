package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.fleetops.tire.tirewindow.DefaultTireQuota
import com.acme.transport.fleetops.tire.tirewindow.defaultTireQuotaBucket

fun summarizeDefaultTireQuotasForMilestone(items: MutableList<DefaultTireQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveTireMilestonesFromQuota(items: MutableList<DefaultTireQuota>): MutableList<TireMilestone> {
    val out = mutableListOf<TireMilestone>()
    for (item in items) {
        val derived = newTireMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeTireMilestone(item: TireMilestone): String = defaultTireQuotaBucket(item.adjustedTotal())
