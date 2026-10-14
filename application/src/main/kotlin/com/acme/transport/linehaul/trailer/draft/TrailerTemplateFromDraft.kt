package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.linehaul.relay.manifest.RelayDraft
import com.acme.transport.linehaul.relay.manifest.normalizeRelayDraftCode

fun summarizeRelayDraftsForTemplate(items: MutableList<RelayDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveTrailerTemplatesFromDraft(items: MutableList<RelayDraft>): MutableList<TrailerTemplate> {
    val out = mutableListOf<TrailerTemplate>()
    for (item in items) {
        val derived = newTrailerTemplate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveTrailerTemplate(item: TrailerTemplate): String = normalizeRelayDraftCode(item.code)
