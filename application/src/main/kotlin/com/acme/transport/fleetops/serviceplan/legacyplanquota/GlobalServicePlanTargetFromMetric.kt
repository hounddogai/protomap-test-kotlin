package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.leasereturn.fallbackreturnforecast.LeaseReturnMetric
import com.acme.transport.fleetops.leasereturn.fallbackreturnforecast.normalizeBatchedLeaseReturnForecastCode

fun summarizeLeaseReturnMetricsForTarget(items: MutableList<LeaseReturnMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalServicePlanTargetsFromMetric(items: MutableList<LeaseReturnMetric>): MutableList<GlobalServicePlanTarget> {
    val out = mutableListOf<GlobalServicePlanTarget>()
    for (item in items) {
        val derived = newGlobalServicePlanTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitGlobalServicePlanTarget(item: GlobalServicePlanTarget): String {
    return normalizeBatchedLeaseReturnForecastCode(item.code)
}
