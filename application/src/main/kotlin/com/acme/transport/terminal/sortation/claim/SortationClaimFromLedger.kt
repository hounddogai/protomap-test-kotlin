package com.acme.transport.terminal.sortation.claim

import com.acme.transport.terminal.dockdoor.ledger.DockDoorLedger
import com.acme.transport.terminal.dockdoor.ledger.classifyDockDoorPlan

fun summarizeDockDoorLedgersForClaim(items: MutableList<DockDoorLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveSortationClaimsFromLedger(items: MutableList<DockDoorLedger>): MutableList<SortationClaim> {
    val out = mutableListOf<SortationClaim>()
    for (item in items) {
        val derived = newSortationClaim(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planSortationClaim(item: SortationClaim): String = classifyDockDoorPlan(item.projectedVolume())
