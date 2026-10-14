package com.acme.transport.network.hub.notice

import com.acme.transport.network.hub.draft.HubDraft
import com.acme.transport.network.hub.draft.classifyGlobalHubLedger

fun summarizeHubDraftsForRecord(items: MutableList<HubDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveLegacyHubRecordsFromDraft(items: MutableList<HubDraft>): MutableList<LegacyHubRecord> {
    val out = mutableListOf<LegacyHubRecord>()
    for (item in items) {
        val derived = newLegacyHubRecord(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeLegacyHubRecord(item: LegacyHubRecord): String = classifyGlobalHubLedger(item.netValue())
