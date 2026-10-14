package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.fleetops.leasereturn.returnentry.ScheduledLeaseReturnEstimate
import com.acme.transport.fleetops.leasereturn.returnentry.normalizeScheduledLeaseReturnEstimateCode

fun summarizeScheduledLeaseReturnEstimatesForMetric(items: MutableList<ScheduledLeaseReturnEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveLeaseReturnMetricsFromEstimate(items: MutableList<ScheduledLeaseReturnEstimate>): MutableList<LeaseReturnMetric> {
    val out = mutableListOf<LeaseReturnMetric>()
    for (item in items) {
        val derived = newLeaseReturnMetric(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyLeaseReturnMetric(item: LeaseReturnMetric): String = normalizeScheduledLeaseReturnEstimateCode(item.code)
