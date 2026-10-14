package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.fleetops.serviceplan.notice.FallbackServicePlanBaseline
import com.acme.transport.fleetops.serviceplan.notice.servicePlanScheduleWeight

fun summarizeFallbackServicePlanBaselinesForQuota(items: MutableList<FallbackServicePlanBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveManualServicePlanQuotasFromBaseline(items: MutableList<FallbackServicePlanBaseline>): MutableList<ManualServicePlanQuota> {
    val out = mutableListOf<ManualServicePlanQuota>()
    for (item in items) {
        val derived = newManualServicePlanQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveManualServicePlanQuota(item: ManualServicePlanQuota): Double = servicePlanScheduleWeight(8, true)
