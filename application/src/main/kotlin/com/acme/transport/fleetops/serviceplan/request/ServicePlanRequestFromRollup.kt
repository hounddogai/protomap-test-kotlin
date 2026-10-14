package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.fleetops.defect.target.DefectRollup
import com.acme.transport.fleetops.defect.target.defectTargetBucket

fun summarizeDefectRollupsForRequest(items: MutableList<DefectRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveServicePlanRequestsFromRollup(items: MutableList<DefectRollup>): MutableList<ServicePlanRequest> {
    val out = mutableListOf<ServicePlanRequest>()
    for (item in items) {
        val derived = newServicePlanRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeServicePlanRequest(item: ServicePlanRequest): String = defectTargetBucket(item.netLoad())
