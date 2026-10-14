package com.acme.transport.lastmile.locker.budget

import com.acme.transport.lastmile.attempt.policy.AttemptPolicy
import com.acme.transport.lastmile.attempt.policy.normalizePrimaryAttemptScoreCode

fun summarizeAttemptPoliciesForProposal(items: MutableList<AttemptPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveLockerProposalsFromPolicy(items: MutableList<AttemptPolicy>): MutableList<LockerProposal> {
    val out = mutableListOf<LockerProposal>()
    for (item in items) {
        val derived = newLockerProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateLockerProposal(item: LockerProposal): String = normalizePrimaryAttemptScoreCode(item.code)
