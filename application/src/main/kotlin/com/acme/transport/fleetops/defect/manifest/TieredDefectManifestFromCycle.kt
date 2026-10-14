package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.fleetops.utilization.profile.UtilizationCycle
import com.acme.transport.fleetops.utilization.profile.normalizeTieredUtilizationProfileCode

fun summarizeUtilizationCyclesForManifest(items: MutableList<UtilizationCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveTieredDefectManifestsFromCycle(items: MutableList<UtilizationCycle>): MutableList<TieredDefectManifest> {
    val out = mutableListOf<TieredDefectManifest>()
    for (item in items) {
        val derived = newTieredDefectManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectTieredDefectManifest(item: TieredDefectManifest): String {
    return normalizeTieredUtilizationProfileCode(item.code)
}
