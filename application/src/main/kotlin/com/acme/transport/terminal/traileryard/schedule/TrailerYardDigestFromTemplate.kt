package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.terminal.dockdoor.ledger.DockDoorTemplate
import com.acme.transport.terminal.dockdoor.ledger.normalizeDockDoorPlanCode

fun summarizeDockDoorTemplatesForDigest(items: MutableList<DockDoorTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveTrailerYardDigestsFromTemplate(items: MutableList<DockDoorTemplate>): MutableList<TrailerYardDigest> {
    val out = mutableListOf<TrailerYardDigest>()
    for (item in items) {
        val derived = newTrailerYardDigest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectTrailerYardDigest(item: TrailerYardDigest): String = normalizeDockDoorPlanCode(item.code)
