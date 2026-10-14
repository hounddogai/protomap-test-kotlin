package com.acme.transport.network.hub.estimate

import com.acme.transport.network.hub.proposal.DefaultHubEntry
import com.acme.transport.network.hub.proposal.normalizeFallbackHubMappingCode

fun summarizeDefaultHubEntriesForBaseline(items: MutableList<DefaultHubEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveLenientHubBaselinesFromEntry(items: MutableList<DefaultHubEntry>): MutableList<LenientHubBaseline> {
    val out = mutableListOf<LenientHubBaseline>()
    for (item in items) {
        val derived = newLenientHubBaseline(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveLenientHubBaseline(item: LenientHubBaseline): String = normalizeFallbackHubMappingCode(item.code)
