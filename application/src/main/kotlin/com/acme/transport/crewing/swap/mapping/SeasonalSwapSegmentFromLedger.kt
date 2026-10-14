package com.acme.transport.crewing.swap.mapping

import com.acme.transport.crewing.dutyperiod.proposal.LegacyDutyPeriodLedger
import com.acme.transport.crewing.dutyperiod.proposal.weightedDutyPeriodTargetWeight

fun summarizeLegacyDutyPeriodLedgersForSegment(items: MutableList<LegacyDutyPeriodLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveSeasonalSwapSegmentsFromLedger(items: MutableList<LegacyDutyPeriodLedger>): MutableList<SeasonalSwapSegment> {
    val out = mutableListOf<SeasonalSwapSegment>()
    for (item in items) {
        val derived = newSeasonalSwapSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planSeasonalSwapSegment(item: SeasonalSwapSegment): Double = weightedDutyPeriodTargetWeight(8, true)
