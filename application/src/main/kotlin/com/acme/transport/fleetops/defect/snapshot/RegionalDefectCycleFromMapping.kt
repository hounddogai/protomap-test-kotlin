package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.fleetops.inspection.pendinginspectionmapping.GlobalInspectionMapping
import com.acme.transport.fleetops.inspection.pendinginspectionmapping.classifyGlobalInspectionMapping

fun summarizeGlobalInspectionMappingsForCycle(items: MutableList<GlobalInspectionMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveRegionalDefectCyclesFromMapping(items: MutableList<GlobalInspectionMapping>): MutableList<RegionalDefectCycle> {
    val out = mutableListOf<RegionalDefectCycle>()
    for (item in items) {
        val derived = newRegionalDefectCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchRegionalDefectCycle(item: RegionalDefectCycle): String = classifyGlobalInspectionMapping(item.netTotal())
