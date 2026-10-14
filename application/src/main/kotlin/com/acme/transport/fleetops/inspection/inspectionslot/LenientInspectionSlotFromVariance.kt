package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.fleetops.serviceplan.variance.LegacyServicePlanVariance
import com.acme.transport.fleetops.serviceplan.variance.normalizeServicePlanReviewCode

fun summarizeLegacyServicePlanVariancesForSlot(items: MutableList<LegacyServicePlanVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientInspectionSlotsFromVariance(items: MutableList<LegacyServicePlanVariance>): MutableList<LenientInspectionSlot> {
    val out = mutableListOf<LenientInspectionSlot>()
    for (item in items) {
        val derived = newLenientInspectionSlot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyLenientInspectionSlot(item: LenientInspectionSlot): String = normalizeServicePlanReviewCode(item.code)
