package com.acme.transport.lastmile.attempt.plan

import com.acme.transport.lastmile.attempt.policy.PrimaryAttemptScore
import com.acme.transport.lastmile.attempt.policy.normalizePrimaryAttemptScoreCode

fun summarizePrimaryAttemptScoresForBaseline(items: MutableList<PrimaryAttemptScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveTieredAttemptBaselinesFromScore(items: MutableList<PrimaryAttemptScore>): MutableList<TieredAttemptBaseline> {
    val out = mutableListOf<TieredAttemptBaseline>()
    for (item in items) {
        val derived = newTieredAttemptBaseline(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeTieredAttemptBaseline(item: TieredAttemptBaseline): String {
    return normalizePrimaryAttemptScoreCode(item.code)
}
