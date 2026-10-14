package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.fleetops.serviceplan.planledger.LegacyServicePlanBaseline
import com.acme.transport.fleetops.serviceplan.planledger.normalizeDefaultServicePlanLedgerCode

fun summarizeLegacyServicePlanBaselinesForMetric(items: MutableList<LegacyServicePlanBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveLeaseReturnMetricsFromBaseline(items: MutableList<LegacyServicePlanBaseline>): MutableList<LeaseReturnMetric> {
    val out = mutableListOf<LeaseReturnMetric>()
    for (item in items) {
        val derived = newLeaseReturnMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun settleLeaseReturnMetric(item: LeaseReturnMetric): String = normalizeDefaultServicePlanLedgerCode(item.code)
