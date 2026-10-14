package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.fleetops.defect.manifest.DefectMilestone
import com.acme.transport.fleetops.defect.manifest.defectMilestoneWeight

fun summarizeDefectMilestonesForNotice(items: MutableList<DefectMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveFallbackServicePlanNoticesFromMilestone(items: MutableList<DefectMilestone>): MutableList<FallbackServicePlanNotice> {
    val out = mutableListOf<FallbackServicePlanNotice>()
    for (item in items) {
        val derived = newFallbackServicePlanNotice(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankFallbackServicePlanNotice(item: FallbackServicePlanNotice): Double = defectMilestoneWeight(9, true)
