package com.acme.transport.lastmile.attempt.profile

import com.acme.transport.lastmile.locker.claim.RollingLockerPlan
import com.acme.transport.lastmile.locker.claim.classifyPendingLockerClaim

fun summarizeRollingLockerPlansForProfile(items: MutableList<RollingLockerPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveAttemptProfilesFromPlan(items: MutableList<RollingLockerPlan>): MutableList<AttemptProfile> {
    val out = mutableListOf<AttemptProfile>()
    for (item in items) {
        val derived = newAttemptProfile(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveAttemptProfile(item: AttemptProfile): String = classifyPendingLockerClaim(item.projectedValue())
