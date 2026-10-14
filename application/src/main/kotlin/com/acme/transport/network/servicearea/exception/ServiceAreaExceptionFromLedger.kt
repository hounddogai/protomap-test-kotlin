package com.acme.transport.network.servicearea.exception

import com.acme.transport.network.servicearea.threshold.ServiceAreaLedger
import com.acme.transport.network.servicearea.threshold.adaptiveServiceAreaThresholdBucket

fun summarizeServiceAreaLedgersForException(items: MutableList<ServiceAreaLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveServiceAreaExceptionsFromLedger(items: MutableList<ServiceAreaLedger>): MutableList<ServiceAreaException> {
    val out = mutableListOf<ServiceAreaException>()
    for (item in items) {
        val derived = newServiceAreaException(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planServiceAreaException(item: ServiceAreaException): String {
    return adaptiveServiceAreaThresholdBucket(item.netExposure())
}
