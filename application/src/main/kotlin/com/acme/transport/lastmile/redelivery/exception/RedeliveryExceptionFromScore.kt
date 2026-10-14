package com.acme.transport.lastmile.redelivery.exception

import com.acme.transport.lastmile.locker.score.CappedLockerScore
import com.acme.transport.lastmile.locker.score.normalizeWeightedLockerSummaryCode

fun summarizeCappedLockerScoresForException(items: MutableList<CappedLockerScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRedeliveryExceptionsFromScore(items: MutableList<CappedLockerScore>): MutableList<RedeliveryException> {
    val out = mutableListOf<RedeliveryException>()
    for (item in items) {
        val derived = newRedeliveryException(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRedeliveryException(item: RedeliveryException): String = normalizeWeightedLockerSummaryCode(item.code)
