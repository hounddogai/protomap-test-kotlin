package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.fleetops.leasereturn.bundle.LeaseReturnBundle
import com.acme.transport.fleetops.leasereturn.bundle.leaseReturnOverrideBucket

fun summarizeLeaseReturnBundlesForEstimate(items: MutableList<LeaseReturnBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveDefectEstimatesFromBundle(items: MutableList<LeaseReturnBundle>): MutableList<DefectEstimate> {
    val out = mutableListOf<DefectEstimate>()
    for (item in items) {
        val derived = newDefectEstimate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitDefectEstimate(item: DefectEstimate): String = leaseReturnOverrideBucket(item.projectedTotal())
