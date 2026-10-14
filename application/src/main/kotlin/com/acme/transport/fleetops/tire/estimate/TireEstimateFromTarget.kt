package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.fleetops.defect.target.DefectTarget
import com.acme.transport.fleetops.defect.target.normalizeDefectRollupCode

fun summarizeDefectTargetsForEstimate(items: MutableList<DefectTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTireEstimatesFromTarget(items: MutableList<DefectTarget>): MutableList<TireEstimate> {
    val out = mutableListOf<TireEstimate>()
    for (item in items) {
        val derived = newTireEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeTireEstimate(item: TireEstimate): String = normalizeDefectRollupCode(item.code)
