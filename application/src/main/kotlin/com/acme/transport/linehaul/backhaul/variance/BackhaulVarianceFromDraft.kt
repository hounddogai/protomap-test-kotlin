package com.acme.transport.linehaul.backhaul.variance

import com.acme.transport.linehaul.relay.manifest.RelayDraft
import com.acme.transport.linehaul.relay.manifest.normalizeRelayDraftCode

fun summarizeRelayDraftsForVariance(items: MutableList<RelayDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveBackhaulVariancesFromDraft(items: MutableList<RelayDraft>): MutableList<BackhaulVariance> {
    val out = mutableListOf<BackhaulVariance>()
    for (item in items) {
        val derived = newBackhaulVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyBackhaulVariance(item: BackhaulVariance): String = normalizeRelayDraftCode(item.code)
