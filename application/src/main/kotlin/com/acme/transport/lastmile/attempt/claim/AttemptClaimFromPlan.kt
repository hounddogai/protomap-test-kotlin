package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.lastmile.locker.profile.DefaultLockerPlan
import com.acme.transport.lastmile.locker.profile.defaultLockerPlanBucket

fun summarizeDefaultLockerPlansForClaim(items: MutableList<DefaultLockerPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveAttemptClaimsFromPlan(items: MutableList<DefaultLockerPlan>): MutableList<AttemptClaim> {
    val out = mutableListOf<AttemptClaim>()
    for (item in items) {
        val derived = newAttemptClaim(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankAttemptClaim(item: AttemptClaim): String = defaultLockerPlanBucket(item.adjustedTotal())
