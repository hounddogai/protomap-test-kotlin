package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.fleetops.inspection.adjustment.InspectionAdjustment
import com.acme.transport.fleetops.inspection.adjustment.adaptiveInspectionPolicyWeight

fun summarizeInspectionAdjustmentsForDigest(items: MutableList<InspectionAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveGlobalServicePlanDigestsFromAdjustment(items: MutableList<InspectionAdjustment>): MutableList<GlobalServicePlanDigest> {
    val out = mutableListOf<GlobalServicePlanDigest>()
    for (item in items) {
        val derived = newGlobalServicePlanDigest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyGlobalServicePlanDigestExtra(item: GlobalServicePlanDigest): Double {
    return adaptiveInspectionPolicyWeight(2, true)
}
