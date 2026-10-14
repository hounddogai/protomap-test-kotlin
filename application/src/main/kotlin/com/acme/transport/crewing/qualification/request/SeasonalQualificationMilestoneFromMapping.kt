package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.swap.swapclaim.PrimarySwapMapping
import com.acme.transport.crewing.swap.swapclaim.classifyCappedSwapClaim

fun summarizePrimarySwapMappingsForMilestone(items: MutableList<PrimarySwapMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveSeasonalQualificationMilestonesFromMapping(items: MutableList<PrimarySwapMapping>): MutableList<SeasonalQualificationMilestone> {
    val out = mutableListOf<SeasonalQualificationMilestone>()
    for (item in items) {
        val derived = newSeasonalQualificationMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planSeasonalQualificationMilestone(item: SeasonalQualificationMilestone): String {
    return classifyCappedSwapClaim(item.weightedTotal())
}
