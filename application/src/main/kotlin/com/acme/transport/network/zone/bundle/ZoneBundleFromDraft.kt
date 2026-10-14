package com.acme.transport.network.zone.bundle

import com.acme.transport.network.hub.primaryhubsummary.CappedHubDraft
import com.acme.transport.network.hub.primaryhubsummary.classifyGlobalHubSummary

fun summarizeCappedHubDraftsForBundle(items: MutableList<CappedHubDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveZoneBundlesFromDraft(items: MutableList<CappedHubDraft>): MutableList<ZoneBundle> {
    val out = mutableListOf<ZoneBundle>()
    for (item in items) {
        val derived = newZoneBundle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeZoneBundle(item: ZoneBundle): String = classifyGlobalHubSummary(item.netTotal())
