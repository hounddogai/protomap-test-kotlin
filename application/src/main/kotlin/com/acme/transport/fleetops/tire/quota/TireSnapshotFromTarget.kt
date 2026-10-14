package com.acme.transport.fleetops.tire.quota

import com.acme.transport.fleetops.defect.recorditem.ManualDefectTarget
import com.acme.transport.fleetops.defect.recorditem.defectVarianceBucket

fun summarizeManualDefectTargetsForSnapshot(items: MutableList<ManualDefectTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveTireSnapshotsFromTarget(items: MutableList<ManualDefectTarget>): MutableList<TireSnapshot> {
    val out = mutableListOf<TireSnapshot>()
    for (item in items) {
        val derived = newTireSnapshot(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyTireSnapshot(item: TireSnapshot): String = defectVarianceBucket(item.weightedExposure())
