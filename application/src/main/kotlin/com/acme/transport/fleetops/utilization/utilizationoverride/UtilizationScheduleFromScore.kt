package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.fleetops.defect.proposal.DefectScore
import com.acme.transport.fleetops.defect.proposal.defectProposalBucket

fun summarizeDefectScoresForSchedule(items: MutableList<DefectScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveUtilizationSchedulesFromScore(items: MutableList<DefectScore>): MutableList<UtilizationSchedule> {
    val out = mutableListOf<UtilizationSchedule>()
    for (item in items) {
        val derived = newUtilizationSchedule(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileUtilizationSchedule(item: UtilizationSchedule): String = defectProposalBucket(item.projectedValue())
