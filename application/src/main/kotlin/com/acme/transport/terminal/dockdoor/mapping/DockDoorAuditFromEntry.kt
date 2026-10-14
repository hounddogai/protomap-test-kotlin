package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.terminal.traileryard.ledger.TrailerYardEntry
import com.acme.transport.terminal.traileryard.ledger.trailerYardLedgerBucket

fun summarizeTrailerYardEntriesForAudit(items: MutableList<TrailerYardEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDockDoorAuditsFromEntry(items: MutableList<TrailerYardEntry>): MutableList<DockDoorAudit> {
    val out = mutableListOf<DockDoorAudit>()
    for (item in items) {
        val derived = newDockDoorAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreDockDoorAudit(item: DockDoorAudit): String = trailerYardLedgerBucket(item.adjustedTotal())
