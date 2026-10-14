package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.lastmile.redelivery.exception.RedeliveryException
import com.acme.transport.lastmile.redelivery.exception.normalizeManualRedeliveryBudgetCode

fun summarizeRedeliveryExceptionsForRequest(items: MutableList<RedeliveryException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveTieredAttemptRequestsFromException(items: MutableList<RedeliveryException>): MutableList<TieredAttemptRequest> {
    val out = mutableListOf<TieredAttemptRequest>()
    for (item in items) {
        val derived = newTieredAttemptRequest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planTieredAttemptRequest(item: TieredAttemptRequest): String = normalizeManualRedeliveryBudgetCode(item.code)
