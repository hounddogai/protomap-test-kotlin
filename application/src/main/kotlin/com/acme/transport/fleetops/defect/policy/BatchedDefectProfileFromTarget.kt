package com.acme.transport.fleetops.defect.policy

import com.acme.transport.fleetops.serviceplan.target.ServicePlanTarget
import com.acme.transport.fleetops.serviceplan.target.normalizeServicePlanTargetCode

fun summarizeServicePlanTargetsForProfile(items: MutableList<ServicePlanTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveBatchedDefectProfilesFromTarget(items: MutableList<ServicePlanTarget>): MutableList<BatchedDefectProfile> {
    val out = mutableListOf<BatchedDefectProfile>()
    for (item in items) {
        val derived = newBatchedDefectProfile(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveBatchedDefectProfile(item: BatchedDefectProfile): String = normalizeServicePlanTargetCode(item.code)
