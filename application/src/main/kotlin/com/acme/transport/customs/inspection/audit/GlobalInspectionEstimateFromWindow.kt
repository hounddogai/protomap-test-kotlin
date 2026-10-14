package com.acme.transport.customs.inspection.audit

import com.acme.transport.customs.clearance.request.LegacyClearanceWindow
import com.acme.transport.customs.clearance.request.clearanceRequestBucket

fun summarizeLegacyClearanceWindowsForEstimate(items: MutableList<LegacyClearanceWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveGlobalInspectionEstimatesFromWindow(items: MutableList<LegacyClearanceWindow>): MutableList<GlobalInspectionEstimate> {
    val out = mutableListOf<GlobalInspectionEstimate>()
    for (item in items) {
        val derived = newGlobalInspectionEstimate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleGlobalInspectionEstimate(item: GlobalInspectionEstimate): String {
    return clearanceRequestBucket(item.measuredValue())
}
