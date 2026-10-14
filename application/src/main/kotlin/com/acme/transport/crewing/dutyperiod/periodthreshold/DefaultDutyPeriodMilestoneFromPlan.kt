package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.crewing.restbreak.metric.StrictRestBreakPlan
import com.acme.transport.crewing.restbreak.metric.restBreakMetricWeight

fun summarizeStrictRestBreakPlansForMilestone(items: MutableList<StrictRestBreakPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDefaultDutyPeriodMilestonesFromPlan(items: MutableList<StrictRestBreakPlan>): MutableList<DefaultDutyPeriodMilestone> {
    val out = mutableListOf<DefaultDutyPeriodMilestone>()
    for (item in items) {
        val derived = newDefaultDutyPeriodMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyDefaultDutyPeriodMilestone(item: DefaultDutyPeriodMilestone): Double = restBreakMetricWeight(3, true)
