package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.fleetops.defect.target.DefectTarget
import com.acme.transport.fleetops.defect.target.defectAgreementWeight

fun summarizeDefectTargetsForOverride(items: MutableList<DefectTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveUtilizationOverridesFromTarget(items: MutableList<DefectTarget>): MutableList<UtilizationOverride> {
    val out = mutableListOf<UtilizationOverride>()
    for (item in items) {
        val derived = newUtilizationOverride(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankUtilizationOverride(item: UtilizationOverride): Double = defectAgreementWeight(2, true)
