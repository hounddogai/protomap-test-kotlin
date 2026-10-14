package com.acme.transport.customs.broker.schedule

import com.acme.transport.customs.inspection.overrideitem.InspectionGroup
import com.acme.transport.customs.inspection.overrideitem.inspectionOverrideBucket

fun summarizeInspectionGroupsForPolicy(items: MutableList<InspectionGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveTieredBrokerPoliciesFromGroup(items: MutableList<InspectionGroup>): MutableList<TieredBrokerPolicy> {
    val out = mutableListOf<TieredBrokerPolicy>()
    for (item in items) {
        val derived = newTieredBrokerPolicy(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceTieredBrokerPolicy(item: TieredBrokerPolicy): String = inspectionOverrideBucket(item.weightedValue())
