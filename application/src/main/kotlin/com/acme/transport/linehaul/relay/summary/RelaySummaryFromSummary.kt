package com.acme.transport.linehaul.relay.summary

import com.acme.transport.linehaul.laneplan.summary.ScheduledLanePlanSummary
import com.acme.transport.linehaul.laneplan.summary.scheduledLanePlanSummaryBucket

fun summarizeScheduledLanePlanSummariesForSummary(items: MutableList<ScheduledLanePlanSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveRelaySummariesFromSummary(items: MutableList<ScheduledLanePlanSummary>): MutableList<RelaySummary> {
    val out = mutableListOf<RelaySummary>()
    for (item in items) {
        val derived = newRelaySummary(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitRelaySummary(item: RelaySummary): String = scheduledLanePlanSummaryBucket(item.measuredValue())
