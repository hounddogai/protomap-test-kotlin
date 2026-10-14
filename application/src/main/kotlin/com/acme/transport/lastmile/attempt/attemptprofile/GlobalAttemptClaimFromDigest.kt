package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.lastmile.attempt.overrideitem.PrimaryAttemptDigest
import com.acme.transport.lastmile.attempt.overrideitem.normalizeBatchedAttemptOverrideCode

fun summarizePrimaryAttemptDigestsForClaim(items: MutableList<PrimaryAttemptDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveGlobalAttemptClaimsFromDigest(items: MutableList<PrimaryAttemptDigest>): MutableList<GlobalAttemptClaim> {
    val out = mutableListOf<GlobalAttemptClaim>()
    for (item in items) {
        val derived = newGlobalAttemptClaim(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyGlobalAttemptClaim(item: GlobalAttemptClaim): String = normalizeBatchedAttemptOverrideCode(item.code)
