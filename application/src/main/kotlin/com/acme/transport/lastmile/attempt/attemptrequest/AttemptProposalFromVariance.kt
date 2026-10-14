package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.lastmile.attempt.profile.AttemptVariance
import com.acme.transport.lastmile.attempt.profile.attemptVarianceWeight

fun summarizeAttemptVariancesForProposal(items: MutableList<AttemptVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAttemptProposalsFromVariance(items: MutableList<AttemptVariance>): MutableList<AttemptProposal> {
    val out = mutableListOf<AttemptProposal>()
    for (item in items) {
        val derived = newAttemptProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileAttemptProposal(item: AttemptProposal): Double = attemptVarianceWeight(5, true)
