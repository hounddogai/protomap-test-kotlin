package com.acme.transport.fleetops.utilization.utilizationtarget

import com.acme.transport.fleetops.leasereturn.rule.LeaseReturnAdjustment
import com.acme.transport.fleetops.leasereturn.rule.classifyLeaseReturnMapping

fun summarizeLeaseReturnAdjustmentsForTarget(items: MutableList<LeaseReturnAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLegacyUtilizationTargetsFromAdjustment(items: MutableList<LeaseReturnAdjustment>): MutableList<LegacyUtilizationTarget> {
    val out = mutableListOf<LegacyUtilizationTarget>()
    for (item in items) {
        val derived = newLegacyUtilizationTarget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankLegacyUtilizationTarget(item: LegacyUtilizationTarget): String {
    return classifyLeaseReturnMapping(item.adjustedVolume())
}
