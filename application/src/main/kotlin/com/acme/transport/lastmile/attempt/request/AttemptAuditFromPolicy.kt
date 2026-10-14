package com.acme.transport.lastmile.attempt.request

import com.acme.transport.lastmile.drop.digest.DropPolicy
import com.acme.transport.lastmile.drop.digest.classifyDropDigest

fun summarizeDropPoliciesForAudit(items: MutableList<DropPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAttemptAuditsFromPolicy(items: MutableList<DropPolicy>): MutableList<AttemptAudit> {
    val out = mutableListOf<AttemptAudit>()
    for (item in items) {
        val derived = newAttemptAudit(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectAttemptAudit(item: AttemptAudit): String = classifyDropDigest(item.weightedTotal())
