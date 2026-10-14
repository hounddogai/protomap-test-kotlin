package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.lastmile.attempt.request.AttemptRequest
import com.acme.transport.lastmile.attempt.request.attemptAuditBucket

fun summarizeAttemptRequestsForTarget(items: MutableList<AttemptRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAttemptTargetsFromRequest(items: MutableList<AttemptRequest>): MutableList<AttemptTarget> {
    val out = mutableListOf<AttemptTarget>()
    for (item in items) {
        val derived = newAttemptTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAttemptTarget(item: AttemptTarget): String = attemptAuditBucket(item.measuredValue())
