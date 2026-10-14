package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.customs.hold.rollup.HoldRollup
import com.acme.transport.customs.hold.rollup.classifyHoldForecast

fun summarizeHoldRollupsForDigest(items: MutableList<HoldRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveSeasonalInspectionDigestsFromRollup(items: MutableList<HoldRollup>): MutableList<SeasonalInspectionDigest> {
    val out = mutableListOf<SeasonalInspectionDigest>()
    for (item in items) {
        val derived = newSeasonalInspectionDigest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifySeasonalInspectionDigest(item: SeasonalInspectionDigest): String {
    return classifyHoldForecast(item.projectedVolume())
}
