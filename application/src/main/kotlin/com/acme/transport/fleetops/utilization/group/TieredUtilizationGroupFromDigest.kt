package com.acme.transport.fleetops.utilization.group

import com.acme.transport.fleetops.serviceplan.agreement.LenientServicePlanDigest
import com.acme.transport.fleetops.serviceplan.agreement.classifyLenientServicePlanDigest

fun summarizeLenientServicePlanDigestsForGroup(items: MutableList<LenientServicePlanDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTieredUtilizationGroupsFromDigest(items: MutableList<LenientServicePlanDigest>): MutableList<TieredUtilizationGroup> {
    val out = mutableListOf<TieredUtilizationGroup>()
    for (item in items) {
        val derived = newTieredUtilizationGroup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignTieredUtilizationGroup(item: TieredUtilizationGroup): String {
    return classifyLenientServicePlanDigest(item.measuredValue())
}
