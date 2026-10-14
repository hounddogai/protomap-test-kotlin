package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.lastmile.locker.lockerpreference.FallbackLockerPreference
import com.acme.transport.lastmile.locker.lockerpreference.fallbackLockerPreferenceWeight

fun summarizeFallbackLockerPreferencesForEntry(items: MutableList<FallbackLockerPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun derivePendingAttemptEntriesFromPreference(items: MutableList<FallbackLockerPreference>): MutableList<PendingAttemptEntry> {
    val out = mutableListOf<PendingAttemptEntry>()
    for (item in items) {
        val derived = newPendingAttemptEntry(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizePendingAttemptEntry(item: PendingAttemptEntry): Double = fallbackLockerPreferenceWeight(6, true)
