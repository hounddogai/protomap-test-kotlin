package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.lastmile.attempt.batchedattemptrequest.SeasonalAttemptEstimate
import com.acme.transport.lastmile.attempt.batchedattemptrequest.classifyPendingAttemptEntry

fun summarizeSeasonalAttemptEstimatesForThreshold(items: MutableList<SeasonalAttemptEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun derivePrimaryDropThresholdsFromEstimate(items: MutableList<SeasonalAttemptEstimate>): MutableList<PrimaryDropThreshold> {
    val out = mutableListOf<PrimaryDropThreshold>()
    for (item in items) {
        val derived = newPrimaryDropThreshold(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyPrimaryDropThreshold(item: PrimaryDropThreshold): String {
    return classifyPendingAttemptEntry(item.projectedTotal())
}
