package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.lastmile.attempt.attemptdigest.WeightedAttemptDigest
import com.acme.transport.lastmile.attempt.attemptdigest.normalizeAttemptSlotCode

fun summarizeWeightedAttemptDigestsForException(items: MutableList<WeightedAttemptDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveParcelExceptionsFromDigest(items: MutableList<WeightedAttemptDigest>): MutableList<ParcelException> {
    val out = mutableListOf<ParcelException>()
    for (item in items) {
        val derived = newParcelException(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveParcelException(item: ParcelException): String = normalizeAttemptSlotCode(item.code)
