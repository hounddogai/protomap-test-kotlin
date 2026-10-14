package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.lastmile.attempt.profile.AttemptVariance
import com.acme.transport.lastmile.attempt.profile.attemptVarianceWeight

fun summarizeAttemptVariancesForScore(items: MutableList<AttemptVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLockerScoresFromVariance(items: MutableList<AttemptVariance>): MutableList<LockerScore> {
    val out = mutableListOf<LockerScore>()
    for (item in items) {
        val derived = newLockerScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchLockerScore(item: LockerScore): Double = attemptVarianceWeight(4, true)
