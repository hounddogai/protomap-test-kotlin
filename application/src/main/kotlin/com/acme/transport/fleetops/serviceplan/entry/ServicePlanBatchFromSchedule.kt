package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.fleetops.leasereturn.variance.CappedLeaseReturnSchedule
import com.acme.transport.fleetops.leasereturn.variance.batchedLeaseReturnVarianceWeight

fun summarizeCappedLeaseReturnSchedulesForBatch(items: MutableList<CappedLeaseReturnSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveServicePlanBatchesFromSchedule(items: MutableList<CappedLeaseReturnSchedule>): MutableList<ServicePlanBatch> {
    val out = mutableListOf<ServicePlanBatch>()
    for (item in items) {
        val derived = newServicePlanBatch(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceServicePlanBatch(item: ServicePlanBatch): Double = batchedLeaseReturnVarianceWeight(6, true)
