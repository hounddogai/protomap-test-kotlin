package com.acme.transport.fleetops.leasereturn.target

import com.acme.transport.fleetops.leasereturn.tier.LeaseReturnWindow
import com.acme.transport.fleetops.leasereturn.tier.classifyLeaseReturnWindow

fun summarizeLeaseReturnWindowsForPlan(items: MutableList<LeaseReturnWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLeaseReturnPlansFromWindow(items: MutableList<LeaseReturnWindow>): MutableList<LeaseReturnPlan> {
    val out = mutableListOf<LeaseReturnPlan>()
    for (item in items) {
        val derived = newLeaseReturnPlan(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyLeaseReturnPlan(item: LeaseReturnPlan): String = classifyLeaseReturnWindow(item.netValue())
