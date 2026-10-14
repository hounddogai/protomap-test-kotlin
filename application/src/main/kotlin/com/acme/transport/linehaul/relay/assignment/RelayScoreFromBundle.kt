package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.linehaul.trailer.policy.TrailerBundle
import com.acme.transport.linehaul.trailer.policy.normalizeTrailerAdjustmentCode

fun summarizeTrailerBundlesForScore(items: MutableList<TrailerBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveRelayScoresFromBundle(items: MutableList<TrailerBundle>): MutableList<RelayScore> {
    val out = mutableListOf<RelayScore>()
    for (item in items) {
        val derived = newRelayScore(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRelayScore(item: RelayScore): String = normalizeTrailerAdjustmentCode(item.code)
