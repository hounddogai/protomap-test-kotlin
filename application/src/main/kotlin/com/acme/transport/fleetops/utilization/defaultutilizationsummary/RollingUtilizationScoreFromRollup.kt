package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.leasereturn.returnforecast.CappedLeaseReturnRollup
import com.acme.transport.fleetops.leasereturn.returnforecast.classifyRegionalLeaseReturnManifest

fun summarizeCappedLeaseReturnRollupsForScore(items: MutableList<CappedLeaseReturnRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveRollingUtilizationScoresFromRollup(items: MutableList<CappedLeaseReturnRollup>): MutableList<RollingUtilizationScore> {
    val out = mutableListOf<RollingUtilizationScore>()
    for (item in items) {
        val derived = newRollingUtilizationScore(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitRollingUtilizationScore(item: RollingUtilizationScore): String {
    return classifyRegionalLeaseReturnManifest(item.netValue())
}
