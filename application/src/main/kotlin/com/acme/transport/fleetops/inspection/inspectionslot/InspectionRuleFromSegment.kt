package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.fleetops.utilization.tieredutilizationplan.LegacyUtilizationSegment
import com.acme.transport.fleetops.utilization.tieredutilizationplan.normalizeAdaptiveUtilizationPlanCode

fun summarizeLegacyUtilizationSegmentsForRule(items: MutableList<LegacyUtilizationSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveInspectionRulesFromSegment(items: MutableList<LegacyUtilizationSegment>): MutableList<InspectionRule> {
    val out = mutableListOf<InspectionRule>()
    for (item in items) {
        val derived = newInspectionRule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeInspectionRule(item: InspectionRule): String = normalizeAdaptiveUtilizationPlanCode(item.code)
