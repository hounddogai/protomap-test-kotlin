package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.inspection.segment.InspectionSegment
import com.acme.transport.fleetops.inspection.segment.manualInspectionGroupWeight

fun summarizeInspectionSegmentsForQuota(items: MutableList<InspectionSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedServicePlanQuotasFromSegment(items: MutableList<InspectionSegment>): MutableList<CappedServicePlanQuota> {
    val out = mutableListOf<CappedServicePlanQuota>()
    for (item in items) {
        val derived = newCappedServicePlanQuota(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectCappedServicePlanQuota(item: CappedServicePlanQuota): Double = manualInspectionGroupWeight(6, true)
