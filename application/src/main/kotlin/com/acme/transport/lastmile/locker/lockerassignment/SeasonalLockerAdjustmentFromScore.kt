package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.lastmile.locker.score.CappedLockerScore
import com.acme.transport.lastmile.locker.score.normalizeWeightedLockerSummaryCode

fun summarizeCappedLockerScoresForAdjustment(items: MutableList<CappedLockerScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveSeasonalLockerAdjustmentsFromScore(items: MutableList<CappedLockerScore>): MutableList<SeasonalLockerAdjustment> {
    val out = mutableListOf<SeasonalLockerAdjustment>()
    for (item in items) {
        val derived = newSeasonalLockerAdjustment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectSeasonalLockerAdjustment(item: SeasonalLockerAdjustment): String {
    return normalizeWeightedLockerSummaryCode(item.code)
}
