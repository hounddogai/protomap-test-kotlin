package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.terminal.traileryard.yardquota.CappedTrailerYardRollup
import com.acme.transport.terminal.traileryard.yardquota.classifyBatchedTrailerYardQuota

fun summarizeCappedTrailerYardRollupsForSnapshot(items: MutableList<CappedTrailerYardRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun derivePrimarySortationSnapshotsFromRollup(items: MutableList<CappedTrailerYardRollup>): MutableList<PrimarySortationSnapshot> {
    val out = mutableListOf<PrimarySortationSnapshot>()
    for (item in items) {
        val derived = newPrimarySortationSnapshot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun derivePrimarySortationSnapshot(item: PrimarySortationSnapshot): String {
    return classifyBatchedTrailerYardQuota(item.netVolume())
}
