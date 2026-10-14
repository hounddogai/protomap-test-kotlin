package com.acme.transport.fleetops.defect.cycle

import com.acme.transport.fleetops.defect.tier.DefectTier
import com.acme.transport.fleetops.defect.tier.defectTierBucket

fun summarizeDefectTiersForCycle(items: MutableList<DefectTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyDefectCyclesFromTier(items: MutableList<DefectTier>): MutableList<LegacyDefectCycle> {
    val out = mutableListOf<LegacyDefectCycle>()
    for (item in items) {
        val derived = newLegacyDefectCycle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankLegacyDefectCycle(item: LegacyDefectCycle): String = defectTierBucket(item.netValue())
