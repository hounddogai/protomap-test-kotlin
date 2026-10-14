package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.lastmile.redelivery.proposal.ScheduledRedeliveryDigest
import com.acme.transport.lastmile.redelivery.proposal.normalizeScheduledRedeliveryDigestCode

fun summarizeScheduledRedeliveryDigestsForAssignment(items: MutableList<ScheduledRedeliveryDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveAttemptAssignmentsFromDigest(items: MutableList<ScheduledRedeliveryDigest>): MutableList<AttemptAssignment> {
    val out = mutableListOf<AttemptAssignment>()
    for (item in items) {
        val derived = newAttemptAssignment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleAttemptAssignmentExtra(item: AttemptAssignment): String {
    return normalizeScheduledRedeliveryDigestCode(item.code)
}
