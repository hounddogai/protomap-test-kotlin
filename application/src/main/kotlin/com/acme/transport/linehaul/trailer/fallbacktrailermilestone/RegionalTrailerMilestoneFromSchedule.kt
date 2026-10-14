package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.linehaul.capacity.capacitysnapshot.GlobalCapacitySchedule
import com.acme.transport.linehaul.capacity.capacitysnapshot.defaultCapacityPreferenceBucket

fun summarizeGlobalCapacitySchedulesForMilestone(items: MutableList<GlobalCapacitySchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveRegionalTrailerMilestonesFromSchedule(items: MutableList<GlobalCapacitySchedule>): MutableList<RegionalTrailerMilestone> {
    val out = mutableListOf<RegionalTrailerMilestone>()
    for (item in items) {
        val derived = newRegionalTrailerMilestone(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateRegionalTrailerMilestone(item: RegionalTrailerMilestone): String {
    return defaultCapacityPreferenceBucket(item.measuredValue())
}
