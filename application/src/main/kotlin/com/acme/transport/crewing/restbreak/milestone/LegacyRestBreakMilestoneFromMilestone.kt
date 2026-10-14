package com.acme.transport.crewing.restbreak.milestone

import com.acme.transport.crewing.qualification.request.SeasonalQualificationMilestone
import com.acme.transport.crewing.qualification.request.qualificationRequestWeight

fun summarizeSeasonalQualificationMilestonesForMilestone(items: MutableList<SeasonalQualificationMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveLegacyRestBreakMilestonesFromMilestone(items: MutableList<SeasonalQualificationMilestone>): MutableList<LegacyRestBreakMilestone> {
    val out = mutableListOf<LegacyRestBreakMilestone>()
    for (item in items) {
        val derived = newLegacyRestBreakMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveLegacyRestBreakMilestone(item: LegacyRestBreakMilestone): Double = qualificationRequestWeight(4, true)
