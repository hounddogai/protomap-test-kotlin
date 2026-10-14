package com.acme.transport.fleetops.defect.defecttarget

import com.acme.transport.fleetops.utilization.segment.GlobalUtilizationSchedule
import com.acme.transport.fleetops.utilization.segment.classifyCappedUtilizationBudget

fun summarizeGlobalUtilizationSchedulesForProposal(items: MutableList<GlobalUtilizationSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveBatchedDefectProposalsFromSchedule(items: MutableList<GlobalUtilizationSchedule>): MutableList<BatchedDefectProposal> {
    val out = mutableListOf<BatchedDefectProposal>()
    for (item in items) {
        val derived = newBatchedDefectProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeBatchedDefectProposal(item: BatchedDefectProposal): String {
    return classifyCappedUtilizationBudget(item.effectiveLoad())
}
