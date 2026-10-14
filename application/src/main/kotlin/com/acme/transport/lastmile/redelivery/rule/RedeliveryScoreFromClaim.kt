package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.lastmile.attempt.attemptprofile.GlobalAttemptClaim
import com.acme.transport.lastmile.attempt.attemptprofile.classifyWeightedAttemptProfile

fun summarizeGlobalAttemptClaimsForScore(items: MutableList<GlobalAttemptClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRedeliveryScoresFromClaim(items: MutableList<GlobalAttemptClaim>): MutableList<RedeliveryScore> {
    val out = mutableListOf<RedeliveryScore>()
    for (item in items) {
        val derived = newRedeliveryScore(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchRedeliveryScore(item: RedeliveryScore): String = classifyWeightedAttemptProfile(item.netExposure())
