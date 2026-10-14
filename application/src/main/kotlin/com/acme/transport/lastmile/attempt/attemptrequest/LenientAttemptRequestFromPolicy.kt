package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.lastmile.drop.dropnotice.StrictDropPolicy
import com.acme.transport.lastmile.drop.dropnotice.classifyRollingDropNotice

fun summarizeStrictDropPoliciesForRequest(items: MutableList<StrictDropPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLenientAttemptRequestsFromPolicy(items: MutableList<StrictDropPolicy>): MutableList<LenientAttemptRequest> {
    val out = mutableListOf<LenientAttemptRequest>()
    for (item in items) {
        val derived = newLenientAttemptRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleLenientAttemptRequest(item: LenientAttemptRequest): String {
    return classifyRollingDropNotice(item.adjustedTotal())
}
