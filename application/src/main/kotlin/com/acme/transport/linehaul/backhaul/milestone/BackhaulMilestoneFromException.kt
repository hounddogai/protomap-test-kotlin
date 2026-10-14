package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.linehaul.capacity.allocation.CapacityException
import com.acme.transport.linehaul.capacity.allocation.capacityExceptionWeight

fun summarizeCapacityExceptionsForMilestone(items: MutableList<CapacityException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveBackhaulMilestonesFromException(items: MutableList<CapacityException>): MutableList<BackhaulMilestone> {
    val out = mutableListOf<BackhaulMilestone>()
    for (item in items) {
        val derived = newBackhaulMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveBackhaulMilestone(item: BackhaulMilestone): Double = capacityExceptionWeight(2, true)
