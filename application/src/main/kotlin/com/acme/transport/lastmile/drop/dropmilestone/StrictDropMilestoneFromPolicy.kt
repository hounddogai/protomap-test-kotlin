package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.lastmile.attempt.policy.AttemptPolicy
import com.acme.transport.lastmile.attempt.policy.normalizePrimaryAttemptScoreCode

fun summarizeAttemptPoliciesForMilestone(items: MutableList<AttemptPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveStrictDropMilestonesFromPolicy(items: MutableList<AttemptPolicy>): MutableList<StrictDropMilestone> {
    val out = mutableListOf<StrictDropMilestone>()
    for (item in items) {
        val derived = newStrictDropMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreStrictDropMilestone(item: StrictDropMilestone): String = normalizePrimaryAttemptScoreCode(item.code)
