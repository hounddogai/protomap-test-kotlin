package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.terminal.sortation.sortationreview.PrimarySortationSnapshot
import com.acme.transport.terminal.sortation.sortationreview.legacySortationReviewWeight

fun summarizePrimarySortationSnapshotsForPolicy(items: MutableList<PrimarySortationSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveStrictDockDoorPoliciesFromSnapshot(items: MutableList<PrimarySortationSnapshot>): MutableList<StrictDockDoorPolicy> {
    val out = mutableListOf<StrictDockDoorPolicy>()
    for (item in items) {
        val derived = newStrictDockDoorPolicy(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankStrictDockDoorPolicy(item: StrictDockDoorPolicy): Double = legacySortationReviewWeight(8, true)
