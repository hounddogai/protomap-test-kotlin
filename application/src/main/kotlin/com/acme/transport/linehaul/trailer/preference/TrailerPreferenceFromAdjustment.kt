package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.linehaul.trailer.policy.TrailerAdjustment
import com.acme.transport.linehaul.trailer.policy.trailerPolicyBucket

fun summarizeTrailerAdjustmentsForPreference(items: MutableList<TrailerAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTrailerPreferencesFromAdjustment(items: MutableList<TrailerAdjustment>): MutableList<TrailerPreference> {
    val out = mutableListOf<TrailerPreference>()
    for (item in items) {
        val derived = newTrailerPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeTrailerPreference(item: TrailerPreference): String = trailerPolicyBucket(item.adjustedValue())
