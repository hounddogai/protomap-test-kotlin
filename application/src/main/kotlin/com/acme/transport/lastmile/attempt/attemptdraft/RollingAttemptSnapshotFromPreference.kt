package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.lastmile.redelivery.claim.PrimaryRedeliveryPreference
import com.acme.transport.lastmile.redelivery.claim.normalizePrimaryRedeliveryPreferenceCode

fun summarizePrimaryRedeliveryPreferencesForSnapshot(items: MutableList<PrimaryRedeliveryPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveRollingAttemptSnapshotsFromPreference(items: MutableList<PrimaryRedeliveryPreference>): MutableList<RollingAttemptSnapshot> {
    val out = mutableListOf<RollingAttemptSnapshot>()
    for (item in items) {
        val derived = newRollingAttemptSnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveRollingAttemptSnapshot(item: RollingAttemptSnapshot): String {
    return normalizePrimaryRedeliveryPreferenceCode(item.code)
}
