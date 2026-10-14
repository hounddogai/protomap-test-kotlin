package com.acme.transport.customs.declaration.milestone

import com.acme.transport.customs.inspection.entry.AdaptiveInspectionProfile
import com.acme.transport.customs.inspection.entry.globalInspectionScheduleBucket

fun summarizeAdaptiveInspectionProfilesForMilestone(items: MutableList<AdaptiveInspectionProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveAdaptiveDeclarationMilestonesFromProfile(items: MutableList<AdaptiveInspectionProfile>): MutableList<AdaptiveDeclarationMilestone> {
    val out = mutableListOf<AdaptiveDeclarationMilestone>()
    for (item in items) {
        val derived = newAdaptiveDeclarationMilestone(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleAdaptiveDeclarationMilestone(item: AdaptiveDeclarationMilestone): String {
    return globalInspectionScheduleBucket(item.effectiveLoad())
}
