package com.acme.transport.lastmile.attempt.request

import com.acme.transport.lastmile.locker.preference.LockerPreference
import com.acme.transport.lastmile.locker.preference.lockerExceptionWeight

fun summarizeLockerPreferencesForAudit(items: MutableList<LockerPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveAttemptAuditsFromPreference(items: MutableList<LockerPreference>): MutableList<AttemptAudit> {
    val out = mutableListOf<AttemptAudit>()
    for (item in items) {
        val derived = newAttemptAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveAttemptAudit(item: AttemptAudit): Double = lockerExceptionWeight(5, true)
