package com.acme.transport.safety.scorecard.assignment

import com.acme.transport.safety.coaching.mapping.DefaultCoachingAgreement
import com.acme.transport.safety.coaching.mapping.defaultCoachingAgreementBucket

fun summarizeDefaultCoachingAgreementsForTarget(items: MutableList<DefaultCoachingAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveFallbackScorecardTargetsFromAgreement(items: MutableList<DefaultCoachingAgreement>): MutableList<FallbackScorecardTarget> {
    val out = mutableListOf<FallbackScorecardTarget>()
    for (item in items) {
        val derived = newFallbackScorecardTarget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitFallbackScorecardTarget(item: FallbackScorecardTarget): String {
    return defaultCoachingAgreementBucket(item.netValue())
}
