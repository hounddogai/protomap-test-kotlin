package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.fleetops.defect.checkpoint.DefectCheckpoint
import com.acme.transport.fleetops.defect.checkpoint.defectExceptionBucket

fun summarizeDefectCheckpointsForTarget(items: MutableList<DefectCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveServicePlanTargetsFromCheckpoint(items: MutableList<DefectCheckpoint>): MutableList<ServicePlanTarget> {
    val out = mutableListOf<ServicePlanTarget>()
    for (item in items) {
        val derived = newServicePlanTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyServicePlanTarget(item: ServicePlanTarget): String = defectExceptionBucket(item.weightedExposure())
