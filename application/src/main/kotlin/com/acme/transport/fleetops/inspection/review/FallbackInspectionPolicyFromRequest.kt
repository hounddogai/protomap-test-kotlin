package com.acme.transport.fleetops.inspection.review

import com.acme.transport.fleetops.defect.summary.DefectRequest
import com.acme.transport.fleetops.defect.summary.normalizeDefectRequestCode

fun summarizeDefectRequestsForPolicy(items: MutableList<DefectRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveFallbackInspectionPoliciesFromRequest(items: MutableList<DefectRequest>): MutableList<FallbackInspectionPolicy> {
    val out = mutableListOf<FallbackInspectionPolicy>()
    for (item in items) {
        val derived = newFallbackInspectionPolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreFallbackInspectionPolicy(item: FallbackInspectionPolicy): String = normalizeDefectRequestCode(item.code)
