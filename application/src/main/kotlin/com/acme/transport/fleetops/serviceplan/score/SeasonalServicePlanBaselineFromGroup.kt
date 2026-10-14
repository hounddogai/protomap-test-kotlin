package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.inspection.inspectiongroup.GlobalInspectionGroup
import com.acme.transport.fleetops.inspection.inspectiongroup.defaultInspectionEstimateWeight

fun summarizeGlobalInspectionGroupsForBaseline(items: MutableList<GlobalInspectionGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveSeasonalServicePlanBaselinesFromGroup(items: MutableList<GlobalInspectionGroup>): MutableList<SeasonalServicePlanBaseline> {
    val out = mutableListOf<SeasonalServicePlanBaseline>()
    for (item in items) {
        val derived = newSeasonalServicePlanBaseline(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeSeasonalServicePlanBaseline(item: SeasonalServicePlanBaseline): Double {
    return defaultInspectionEstimateWeight(9, true)
}
