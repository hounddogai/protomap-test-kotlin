package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.fleetops.tire.tirewindow.RollingTireCycle
import com.acme.transport.fleetops.tire.tirewindow.defaultTireQuotaBucket

fun summarizeRollingTireCyclesForBaseline(items: MutableList<RollingTireCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRegionalLeaseReturnBaselinesFromCycle(items: MutableList<RollingTireCycle>): MutableList<RegionalLeaseReturnBaseline> {
    val out = mutableListOf<RegionalLeaseReturnBaseline>()
    for (item in items) {
        val derived = newRegionalLeaseReturnBaseline(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRegionalLeaseReturnBaseline(item: RegionalLeaseReturnBaseline): String {
    return defaultTireQuotaBucket(item.weightedLoad())
}
