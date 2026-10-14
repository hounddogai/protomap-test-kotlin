package com.acme.transport.lastmile.locker.target

import com.acme.transport.lastmile.attempt.milestone.FallbackAttemptMilestone
import com.acme.transport.lastmile.attempt.milestone.normalizeAttemptLedgerCode

fun summarizeFallbackAttemptMilestonesForTarget(items: MutableList<FallbackAttemptMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryLockerTargetsFromMilestone(items: MutableList<FallbackAttemptMilestone>): MutableList<PrimaryLockerTarget> {
    val out = mutableListOf<PrimaryLockerTarget>()
    for (item in items) {
        val derived = newPrimaryLockerTarget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settlePrimaryLockerTarget(item: PrimaryLockerTarget): String = normalizeAttemptLedgerCode(item.code)
