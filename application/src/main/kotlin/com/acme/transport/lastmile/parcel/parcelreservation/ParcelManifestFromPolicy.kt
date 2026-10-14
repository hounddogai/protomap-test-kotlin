package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.lastmile.attempt.policy.AttemptPolicy
import com.acme.transport.lastmile.attempt.policy.normalizePrimaryAttemptScoreCode

fun summarizeAttemptPoliciesForManifest(items: MutableList<AttemptPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveParcelManifestsFromPolicy(items: MutableList<AttemptPolicy>): MutableList<ParcelManifest> {
    val out = mutableListOf<ParcelManifest>()
    for (item in items) {
        val derived = newParcelManifest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectParcelManifest(item: ParcelManifest): String = normalizePrimaryAttemptScoreCode(item.code)
