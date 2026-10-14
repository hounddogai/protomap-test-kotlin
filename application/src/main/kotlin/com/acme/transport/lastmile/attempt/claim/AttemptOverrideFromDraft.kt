package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.lastmile.parcel.claim.GlobalParcelDraft
import com.acme.transport.lastmile.parcel.claim.parcelAuditWeight

fun summarizeGlobalParcelDraftsForOverride(items: MutableList<GlobalParcelDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAttemptOverridesFromDraft(items: MutableList<GlobalParcelDraft>): MutableList<AttemptOverride> {
    val out = mutableListOf<AttemptOverride>()
    for (item in items) {
        val derived = newAttemptOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankAttemptOverride(item: AttemptOverride): Double = parcelAuditWeight(3, true)
