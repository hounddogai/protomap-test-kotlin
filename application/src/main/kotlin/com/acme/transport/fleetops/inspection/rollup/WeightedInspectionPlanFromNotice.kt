package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.fleetops.defect.defectsummary.SeasonalDefectNotice
import com.acme.transport.fleetops.defect.defectsummary.seasonalDefectNoticeWeight

fun summarizeSeasonalDefectNoticesForPlan(items: MutableList<SeasonalDefectNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveWeightedInspectionPlansFromNotice(items: MutableList<SeasonalDefectNotice>): MutableList<WeightedInspectionPlan> {
    val out = mutableListOf<WeightedInspectionPlan>()
    for (item in items) {
        val derived = newWeightedInspectionPlan(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileWeightedInspectionPlan(item: WeightedInspectionPlan): Double = seasonalDefectNoticeWeight(5, true)
