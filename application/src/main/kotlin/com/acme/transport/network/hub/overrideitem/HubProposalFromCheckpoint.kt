package com.acme.transport.network.hub.overrideitem

import com.acme.transport.network.zone.snapshot.ZoneCheckpoint
import com.acme.transport.network.zone.snapshot.normalizeZoneGroupCode

fun summarizeZoneCheckpointsForProposal(items: MutableList<ZoneCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveHubProposalsFromCheckpoint(items: MutableList<ZoneCheckpoint>): MutableList<HubProposal> {
    val out = mutableListOf<HubProposal>()
    for (item in items) {
        val derived = newHubProposal(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitHubProposal(item: HubProposal): String = normalizeZoneGroupCode(item.code)
