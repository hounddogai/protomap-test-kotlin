package com.acme.transport.fleetops.defect.metric

import com.acme.transport.fleetops.tire.slot.TieredTireTier
import com.acme.transport.fleetops.tire.slot.classifyDefaultTireSchedule

fun summarizeTieredTireTiersForSegment(items: MutableList<TieredTireTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveDefectSegmentsFromTier(items: MutableList<TieredTireTier>): MutableList<DefectSegment> {
    val out = mutableListOf<DefectSegment>()
    for (item in items) {
        val derived = newDefectSegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyDefectSegment(item: DefectSegment): String = classifyDefaultTireSchedule(item.effectiveValue())
