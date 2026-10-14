package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.fleetops.defect.template.AdaptiveDefectTemplate
import com.acme.transport.fleetops.defect.template.classifyAdaptiveDefectTemplate

fun summarizeAdaptiveDefectTemplatesForBaseline(items: MutableList<AdaptiveDefectTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveWeightedInspectionBaselinesFromTemplate(items: MutableList<AdaptiveDefectTemplate>): MutableList<WeightedInspectionBaseline> {
    val out = mutableListOf<WeightedInspectionBaseline>()
    for (item in items) {
        val derived = newWeightedInspectionBaseline(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyWeightedInspectionBaseline(item: WeightedInspectionBaseline): String {
    return classifyAdaptiveDefectTemplate(item.measuredValue())
}
