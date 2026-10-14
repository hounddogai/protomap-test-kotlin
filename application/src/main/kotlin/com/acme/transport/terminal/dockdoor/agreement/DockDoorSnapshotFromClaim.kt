package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.terminal.sortation.claim.SortationClaim
import com.acme.transport.terminal.sortation.claim.normalizeSortationBundleCode

fun summarizeSortationClaimsForSnapshot(items: MutableList<SortationClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDockDoorSnapshotsFromClaim(items: MutableList<SortationClaim>): MutableList<DockDoorSnapshot> {
    val out = mutableListOf<DockDoorSnapshot>()
    for (item in items) {
        val derived = newDockDoorSnapshot(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDockDoorSnapshot(item: DockDoorSnapshot): String = normalizeSortationBundleCode(item.code)
