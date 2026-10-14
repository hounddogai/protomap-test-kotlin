package com.acme.transport.crewing.swap.mapping

import com.acme.transport.crewing.restbreak.tier.CappedRestBreakNotice
import com.acme.transport.crewing.restbreak.tier.defaultRestBreakScoreWeight

fun summarizeCappedRestBreakNoticesForSegment(items: MutableList<CappedRestBreakNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveSeasonalSwapSegmentsFromNotice(items: MutableList<CappedRestBreakNotice>): MutableList<SeasonalSwapSegment> {
    val out = mutableListOf<SeasonalSwapSegment>()
    for (item in items) {
        val derived = newSeasonalSwapSegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateSeasonalSwapSegment(item: SeasonalSwapSegment): Double = defaultRestBreakScoreWeight(2, true)
