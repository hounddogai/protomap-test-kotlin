package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.fleetops.utilization.estimate.CappedUtilizationEstimate
import com.acme.transport.fleetops.utilization.estimate.utilizationPlanBucket

fun summarizeCappedUtilizationEstimatesForNotice(items: MutableList<CappedUtilizationEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveServicePlanNoticesFromEstimate(items: MutableList<CappedUtilizationEstimate>): MutableList<ServicePlanNotice> {
    val out = mutableListOf<ServicePlanNotice>()
    for (item in items) {
        val derived = newServicePlanNotice(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeServicePlanNotice(item: ServicePlanNotice): String = utilizationPlanBucket(item.projectedExposure())
