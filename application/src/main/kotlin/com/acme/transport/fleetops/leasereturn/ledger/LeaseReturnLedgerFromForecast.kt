package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.fleetops.utilization.utilizationtarget.UtilizationForecast
import com.acme.transport.fleetops.utilization.utilizationtarget.legacyUtilizationTargetBucket

fun summarizeUtilizationForecastsForLedger(items: MutableList<UtilizationForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLeaseReturnLedgersFromForecast(items: MutableList<UtilizationForecast>): MutableList<LeaseReturnLedger> {
    val out = mutableListOf<LeaseReturnLedger>()
    for (item in items) {
        val derived = newLeaseReturnLedger(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchLeaseReturnLedger(item: LeaseReturnLedger): String = legacyUtilizationTargetBucket(item.adjustedTotal())
