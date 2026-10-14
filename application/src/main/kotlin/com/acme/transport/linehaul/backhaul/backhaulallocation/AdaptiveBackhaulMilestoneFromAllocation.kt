package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.linehaul.tender.fallbacktenderbatch.RollingTenderAllocation
import com.acme.transport.linehaul.tender.fallbacktenderbatch.rollingTenderAllocationWeight

fun summarizeRollingTenderAllocationsForMilestone(items: MutableList<RollingTenderAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAdaptiveBackhaulMilestonesFromAllocation(items: MutableList<RollingTenderAllocation>): MutableList<AdaptiveBackhaulMilestone> {
    val out = mutableListOf<AdaptiveBackhaulMilestone>()
    for (item in items) {
        val derived = newAdaptiveBackhaulMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitAdaptiveBackhaulMilestone(item: AdaptiveBackhaulMilestone): Double {
    return rollingTenderAllocationWeight(3, true)
}
