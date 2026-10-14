package com.acme.transport.network.transittime.timebatch

import com.acme.transport.network.zone.proposal.ZoneProposal
import com.acme.transport.network.zone.proposal.classifyZoneProposal

fun summarizeZoneProposalsForAudit(items: MutableList<ZoneProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveTransitTimeAuditsFromProposal(items: MutableList<ZoneProposal>): MutableList<TransitTimeAudit> {
    val out = mutableListOf<TransitTimeAudit>()
    for (item in items) {
        val derived = newTransitTimeAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankTransitTimeAudit(item: TransitTimeAudit): String = classifyZoneProposal(item.netLoad())
