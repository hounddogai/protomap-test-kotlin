package com.acme.transport.fleetops.leasereturn.schedule

import com.acme.transport.fleetops.leasereturn.bundle.LeaseReturnOverride
import com.acme.transport.fleetops.leasereturn.bundle.classifyLeaseReturnBundle

fun summarizeLeaseReturnOverridesForSchedule(items: MutableList<LeaseReturnOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLeaseReturnSchedulesFromOverride(items: MutableList<LeaseReturnOverride>): MutableList<LeaseReturnSchedule> {
    val out = mutableListOf<LeaseReturnSchedule>()
    for (item in items) {
        val derived = newLeaseReturnSchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeLeaseReturnSchedule(item: LeaseReturnSchedule): String {
    return classifyLeaseReturnBundle(item.effectiveValue())
}
