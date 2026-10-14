package com.acme.transport.safety.coaching.review

import com.acme.transport.safety.scorecard.profile.ScheduledScorecardMapping
import com.acme.transport.safety.scorecard.profile.manualScorecardTemplateWeight

fun summarizeScheduledScorecardMappingsForLedger(items: MutableList<ScheduledScorecardMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveManualCoachingLedgersFromMapping(items: MutableList<ScheduledScorecardMapping>): MutableList<ManualCoachingLedger> {
    val out = mutableListOf<ManualCoachingLedger>()
    for (item in items) {
        val derived = newManualCoachingLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceManualCoachingLedger(item: ManualCoachingLedger): Double = manualScorecardTemplateWeight(4, true)
