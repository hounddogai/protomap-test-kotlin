package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.freightpay.accessorial.accessorialmetric.BatchedAccessorialReservation
import com.acme.transport.freightpay.accessorial.accessorialmetric.lenientAccessorialMetricBucket

fun summarizeBatchedAccessorialReservationsForPolicy(items: MutableList<BatchedAccessorialReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRollingAccessorialPoliciesFromReservation(items: MutableList<BatchedAccessorialReservation>): MutableList<RollingAccessorialPolicy> {
    val out = mutableListOf<RollingAccessorialPolicy>()
    for (item in items) {
        val derived = newRollingAccessorialPolicy(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeRollingAccessorialPolicy(item: RollingAccessorialPolicy): String {
    return lenientAccessorialMetricBucket(item.adjustedExposure())
}
