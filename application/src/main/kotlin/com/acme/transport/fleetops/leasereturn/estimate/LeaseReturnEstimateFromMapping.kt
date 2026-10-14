package com.acme.transport.fleetops.leasereturn.estimate

import com.acme.transport.fleetops.defect.mapping.DefectMapping
import com.acme.transport.fleetops.defect.mapping.normalizePrimaryDefectBundleCode

fun summarizeDefectMappingsForEstimate(items: MutableList<DefectMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveLeaseReturnEstimatesFromMapping(items: MutableList<DefectMapping>): MutableList<LeaseReturnEstimate> {
    val out = mutableListOf<LeaseReturnEstimate>()
    for (item in items) {
        val derived = newLeaseReturnEstimate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileLeaseReturnEstimate(item: LeaseReturnEstimate): String = normalizePrimaryDefectBundleCode(item.code)
