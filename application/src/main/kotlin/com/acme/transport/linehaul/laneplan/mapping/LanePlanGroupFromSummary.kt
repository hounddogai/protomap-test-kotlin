package com.acme.transport.linehaul.laneplan.mapping

import com.acme.transport.linehaul.laneplan.summary.ScheduledLanePlanSummary
import com.acme.transport.linehaul.laneplan.summary.batchedLanePlanBundleWeight

fun summarizeScheduledLanePlanSummariesForGroup(items: MutableList<ScheduledLanePlanSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveLanePlanGroupsFromSummary(items: MutableList<ScheduledLanePlanSummary>): MutableList<LanePlanGroup> {
    val out = mutableListOf<LanePlanGroup>()
    for (item in items) {
        val derived = newLanePlanGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLanePlanGroup(item: LanePlanGroup): Double = batchedLanePlanBundleWeight(8, true)
