package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.fleetops.leasereturn.variance.BatchedLeaseReturnVariance
import com.acme.transport.fleetops.leasereturn.variance.cappedLeaseReturnScheduleWeight

fun summarizeBatchedLeaseReturnVariancesForBundle(items: MutableList<BatchedLeaseReturnVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveScheduledTireBundlesFromVariance(items: MutableList<BatchedLeaseReturnVariance>): MutableList<ScheduledTireBundle> {
    val out = mutableListOf<ScheduledTireBundle>()
    for (item in items) {
        val derived = newScheduledTireBundle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyScheduledTireBundle(item: ScheduledTireBundle): Double = cappedLeaseReturnScheduleWeight(9, true)
