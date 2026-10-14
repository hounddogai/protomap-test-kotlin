package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.fleetops.defect.tier.DefectCycle
import com.acme.transport.fleetops.defect.tier.normalizeDefectCycleCode

fun summarizeDefectCyclesForManifest(items: MutableList<DefectCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun derivePrimaryTireManifestsFromCycle(items: MutableList<DefectCycle>): MutableList<PrimaryTireManifest> {
    val out = mutableListOf<PrimaryTireManifest>()
    for (item in items) {
        val derived = newPrimaryTireManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolvePrimaryTireManifest(item: PrimaryTireManifest): String = normalizeDefectCycleCode(item.code)
