package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.fleetops.defect.defectproposal.FallbackDefectAssignment
import com.acme.transport.fleetops.defect.defectproposal.normalizeTieredDefectVarianceCode

fun summarizeFallbackDefectAssignmentsForMilestone(items: MutableList<FallbackDefectAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveStrictDefectMilestonesFromAssignment(items: MutableList<FallbackDefectAssignment>): MutableList<StrictDefectMilestone> {
    val out = mutableListOf<StrictDefectMilestone>()
    for (item in items) {
        val derived = newStrictDefectMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveStrictDefectMilestone(item: StrictDefectMilestone): String {
    return normalizeTieredDefectVarianceCode(item.code)
}
