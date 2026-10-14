package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.lastmile.locker.quota.LockerThreshold
import com.acme.transport.lastmile.locker.quota.lockerQuotaWeight

fun summarizeLockerThresholdsForAllocation(items: MutableList<LockerThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRedeliveryAllocationsFromThreshold(items: MutableList<LockerThreshold>): MutableList<RedeliveryAllocation> {
    val out = mutableListOf<RedeliveryAllocation>()
    for (item in items) {
        val derived = newRedeliveryAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileRedeliveryAllocation(item: RedeliveryAllocation): Double = lockerQuotaWeight(1, true)
