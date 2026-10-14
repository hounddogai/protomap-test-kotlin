package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.customs.declaration.profile.PendingDeclarationBaseline
import com.acme.transport.customs.declaration.profile.adaptiveDeclarationVarianceBucket

fun summarizePendingDeclarationBaselinesForMilestone(items: MutableList<PendingDeclarationBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRollingClearanceMilestonesFromBaseline(items: MutableList<PendingDeclarationBaseline>): MutableList<RollingClearanceMilestone> {
    val out = mutableListOf<RollingClearanceMilestone>()
    for (item in items) {
        val derived = newRollingClearanceMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeRollingClearanceMilestone(item: RollingClearanceMilestone): String {
    return adaptiveDeclarationVarianceBucket(item.effectiveVolume())
}
