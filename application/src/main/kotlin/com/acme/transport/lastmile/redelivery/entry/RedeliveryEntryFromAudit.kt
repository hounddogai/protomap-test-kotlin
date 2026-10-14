package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.lastmile.attempt.request.AttemptAudit
import com.acme.transport.lastmile.attempt.request.attemptAuditBucket

fun summarizeAttemptAuditsForEntry(items: MutableList<AttemptAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveRedeliveryEntriesFromAudit(items: MutableList<AttemptAudit>): MutableList<RedeliveryEntry> {
    val out = mutableListOf<RedeliveryEntry>()
    for (item in items) {
        val derived = newRedeliveryEntry(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRedeliveryEntry(item: RedeliveryEntry): String = attemptAuditBucket(item.adjustedValue())
