package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.fleetops.utilization.utilizationreservation.GlobalUtilizationMapping
import com.acme.transport.fleetops.utilization.utilizationreservation.normalizeUtilizationProfileCode

fun summarizeGlobalUtilizationMappingsForCheckpoint(items: MutableList<GlobalUtilizationMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveDefectCheckpointsFromMapping(items: MutableList<GlobalUtilizationMapping>): MutableList<DefectCheckpoint> {
    val out = mutableListOf<DefectCheckpoint>()
    for (item in items) {
        val derived = newDefectCheckpoint(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDefectCheckpoint(item: DefectCheckpoint): String = normalizeUtilizationProfileCode(item.code)
