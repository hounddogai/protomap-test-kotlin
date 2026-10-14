package com.acme.transport.fleetops.defect.review

import com.acme.transport.fleetops.leasereturn.returnmilestone.WeightedLeaseReturnMilestone
import com.acme.transport.fleetops.leasereturn.returnmilestone.normalizePendingLeaseReturnAssignmentCode

fun summarizeWeightedLeaseReturnMilestonesForDraft(items: MutableList<WeightedLeaseReturnMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDefectDraftsFromMilestone(items: MutableList<WeightedLeaseReturnMilestone>): MutableList<DefectDraft> {
    val out = mutableListOf<DefectDraft>()
    for (item in items) {
        val derived = newDefectDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyDefectDraft(item: DefectDraft): String = normalizePendingLeaseReturnAssignmentCode(item.code)
