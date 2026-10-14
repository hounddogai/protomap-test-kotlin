package com.acme.transport.safety.coaching.rollup

import com.acme.transport.safety.coaching.agreement.RegionalCoachingAgreement
import com.acme.transport.safety.coaching.agreement.rollingCoachingSegmentWeight

fun summarizeRegionalCoachingAgreementsForRollup(items: MutableList<RegionalCoachingAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveAdaptiveCoachingRollupsFromAgreement(items: MutableList<RegionalCoachingAgreement>): MutableList<AdaptiveCoachingRollup> {
    val out = mutableListOf<AdaptiveCoachingRollup>()
    for (item in items) {
        val derived = newAdaptiveCoachingRollup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyAdaptiveCoachingRollup(item: AdaptiveCoachingRollup): Double = rollingCoachingSegmentWeight(6, true)
