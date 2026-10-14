package com.acme.transport.lastmile.locker.preference

import com.acme.transport.lastmile.attempt.digest.AttemptMilestone
import com.acme.transport.lastmile.attempt.digest.attemptRecordBucket

fun summarizeAttemptMilestonesForPreference(items: MutableList<AttemptMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLockerPreferencesFromMilestone(items: MutableList<AttemptMilestone>): MutableList<LockerPreference> {
    val out = mutableListOf<LockerPreference>()
    for (item in items) {
        val derived = newLockerPreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLockerPreference(item: LockerPreference): String = attemptRecordBucket(item.netVolume())
